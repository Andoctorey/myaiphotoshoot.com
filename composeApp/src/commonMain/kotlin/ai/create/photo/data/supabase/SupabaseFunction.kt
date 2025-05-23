package ai.create.photo.data.supabase

import co.touchlab.kermit.Logger
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body

object SupabaseFunction {

    suspend fun trainAiModel(steps: Int) {
        Logger.i("trainAiModel, steps: $steps")
        Supabase.supabase.functions.invoke(
            function = "training",
            body = mapOf("steps" to steps)
        )
    }

    suspend fun generatePhoto(trainingId: String, prompt: String, parentGenerationId: String?) {
        Logger.i("generatePhoto trainingId: $trainingId, parentGenerationId: $parentGenerationId, prompt: $prompt")
        Supabase.supabase.functions.invoke(
            function = "generate",
            body = mapOf(
                "training_id" to trainingId,
                "prompt" to prompt,
                "parent_generation_id" to parentGenerationId,
            )
        )
    }

    suspend fun deleteUser() {
        Logger.i("deleteUser")
        Supabase.supabase.functions.invoke(function = "delete-user")
    }

    suspend fun analyzePhoto(fileId: String) {
        Logger.i("analyzePhoto fileId: $fileId")
        Supabase.supabase.functions.invoke(
            function = "analyze-selfie",
            body = mapOf("file_id" to fileId)
        )
    }

    suspend fun generatePersonDescription(trainingId: String) {
        Logger.i("generatePersonDescription, trainingId: $trainingId")
        Supabase.supabase.functions.invoke(
            function = "generate-person-description",
            body = mapOf("training_id" to trainingId)
        )
    }

    suspend fun surpriseMe(): String {
        Logger.i("surpriseMe")
        val response = Supabase.supabase.functions.invoke(function = "surprise-me")
        return response.body<String>()
    }

    suspend fun translate(prompt: String): String {
        Logger.i("translate prompt: $prompt")
        val response = Supabase.supabase.functions.invoke(
            function = "translate",
            body = mapOf("prompt" to prompt)
        )
        return response.body<String>()
    }

    suspend fun enhancePrompt(prompt: String): String {
        Logger.i("enhancePrompt, prompt: $prompt")
        val response = Supabase.supabase.functions.invoke(
            function = "enhance-prompt",
            body = mapOf("prompt" to prompt)
        )
        return response.body<String>()
    }

    suspend fun pictureToPrompt(url: String): String {
        Logger.i("pictureToPrompt url: $url")
        val response = Supabase.supabase.functions.invoke(
            function = "picture-to-prompt",
            body = mapOf("url" to url)
        )
        return response.body<String>()
    }

    suspend fun applyPromoCode(code: String): Boolean {
        Logger.i("enterPromoCode: $code")
        val response = Supabase.supabase.functions.invoke(
            function = "promo-code",
            body = mapOf("promo_code" to code)
        )
        return response.body<Boolean>()
    }

    suspend fun sendSlackError(error: String) {
        Logger.i("sendSlackError: $error")
        Supabase.supabase.functions.invoke(
            function = "slack-error",
            body = mapOf("error" to error)
        )
    }

    suspend fun verifyAndroidPurchase(productId: String, purchaseToken: String): Boolean {
        Logger.i("verifyAndroidPurchase: $productId, $purchaseToken")
        val response = Supabase.supabase.functions.invoke(
            function = "verify-android-purchase",
            body = mapOf("product_id" to productId, "purchase_token" to purchaseToken)
        )
        return response.body<Boolean>()
    }

    suspend fun verifyIosPurchase(
        productId: String,
        receipt: String,
        transactionId: String
    ): Boolean {
        Logger.i("verifyIosPurchase: $productId, $receipt")
        val response = Supabase.supabase.functions.invoke(
            function = "verify-ios-purchase",
            body = mapOf(
                "product_id" to productId,
                "receipt" to receipt,
                "transaction_id" to transactionId
            )
        )
        return response.body<Boolean>()
    }
}