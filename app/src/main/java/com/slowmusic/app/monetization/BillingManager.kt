package com.slowmusic.app.monetization

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.slowmusic.app.domain.model.SubscriptionType
import com.slowmusic.app.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext context: Context
) : PurchasesUpdatedListener {

    private val _availableProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val availableProducts: StateFlow<List<ProductDetails>> = _availableProducts.asStateFlow()

    private val _latestPurchase = MutableStateFlow<SubscriptionType?>(null)
    val latestPurchase: StateFlow<SubscriptionType?> = _latestPurchase.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    suspend fun connect(): BillingResult = suspendCancellableCoroutine { continuation ->
        if (billingClient.isReady) {
            continuation.resume(okResult())
            return@suspendCancellableCoroutine
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                continuation.resume(result)
            }

            override fun onBillingServiceDisconnected() {
                Logger.d("Billing", "Billing service disconnected")
            }
        })
    }

    suspend fun loadProducts(): Result<List<ProductDetails>> {
        val connected = connect()
        if (connected.responseCode != BillingClient.BillingResponseCode.OK) {
            return Result.failure(IllegalStateException(connected.debugMessage))
        }

        return suspendCancellableCoroutine { continuation ->
            val products = PRODUCT_IDS.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(products)
                .build()

            billingClient.queryProductDetailsAsync(params) { result, details ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _availableProducts.value = details
                    continuation.resume(Result.success(details))
                } else {
                    continuation.resume(Result.failure(IllegalStateException(result.debugMessage)))
                }
            }
        }
    }

    suspend fun launchBillingFlow(activity: Activity, type: SubscriptionType): BillingResult {
        if (type == SubscriptionType.FREE) return okResult()
        val products = if (_availableProducts.value.isEmpty()) {
            loadProducts().getOrDefault(emptyList())
        } else {
            _availableProducts.value
        }
        val product = products.firstOrNull { it.productId == productIdFor(type) }
            ?: return errorResult("Product not configured in Play Console: ${productIdFor(type)}")

        val offerToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken
            ?: return errorResult("No subscription offer token for ${product.productId}")

        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product)
            .setOfferToken(offerToken)
            .build()

        return billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(productParams)).build()
        )
    }

    suspend fun syncPurchases(): Result<SubscriptionType> {
        val connected = connect()
        if (connected.responseCode != BillingClient.BillingResponseCode.OK) {
            return Result.failure(IllegalStateException(connected.debugMessage))
        }

        return suspendCancellableCoroutine { continuation ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            billingClient.queryPurchasesAsync(params) { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    val type = purchases.asSequence()
                        .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                        .flatMap { it.products.asSequence() }
                        .mapNotNull { subscriptionTypeForProductId(it) }
                        .firstOrNull() ?: SubscriptionType.FREE
                    _latestPurchase.value = type
                    continuation.resume(Result.success(type))
                } else {
                    continuation.resume(Result.failure(IllegalStateException(result.debugMessage)))
                }
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            val type = purchases.orEmpty()
                .flatMap { it.products }
                .mapNotNull { subscriptionTypeForProductId(it) }
                .firstOrNull()
            _latestPurchase.value = type
            Logger.d("Billing", "Purchase update: $type")
        } else {
            Logger.d("Billing", "Purchase update failed: ${result.debugMessage}")
        }
    }

    private fun productIdFor(type: SubscriptionType): String = when (type) {
        SubscriptionType.PREMIUM -> PREMIUM_PRODUCT_ID
        SubscriptionType.FAMILY -> FAMILY_PRODUCT_ID
        SubscriptionType.STUDENT -> STUDENT_PRODUCT_ID
        SubscriptionType.FREE -> ""
    }

    private fun subscriptionTypeForProductId(productId: String): SubscriptionType? = when (productId) {
        PREMIUM_PRODUCT_ID -> SubscriptionType.PREMIUM
        FAMILY_PRODUCT_ID -> SubscriptionType.FAMILY
        STUDENT_PRODUCT_ID -> SubscriptionType.STUDENT
        else -> null
    }

    private fun okResult(): BillingResult = BillingResult.newBuilder()
        .setResponseCode(BillingClient.BillingResponseCode.OK)
        .build()

    private fun errorResult(message: String): BillingResult = BillingResult.newBuilder()
        .setResponseCode(BillingClient.BillingResponseCode.ERROR)
        .setDebugMessage(message)
        .build()

    companion object {
        const val PREMIUM_PRODUCT_ID = "slow_music_premium_monthly"
        const val FAMILY_PRODUCT_ID = "slow_music_family_monthly"
        const val STUDENT_PRODUCT_ID = "slow_music_student_yearly"
        val PRODUCT_IDS = listOf(PREMIUM_PRODUCT_ID, FAMILY_PRODUCT_ID, STUDENT_PRODUCT_ID)
    }
}
