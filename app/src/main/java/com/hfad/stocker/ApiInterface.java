package com.hfad.stocker;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("/macros/echo?user_content_key=PDRHRQYB2kkkAiYN9PP5XdIBJmyVsENn-jsBDjlaG-S4rXzc6nue8z9e23kqjEWJ690MH7jH_UPCfxSoZCSfC8TW1tsBGZiam5_BxDlH2jW0nuo2oDemN9CCS2h10ox_1xSncGQajx_ryfhECjZEnNiMF7Kxs6WodgH5K51CCaQAf6SyUPwHdTC3oc6wNdguoFN4Uk5iDHknf_1TdltNW2cgAyQImGwyr0gBADE2B1kqhx1pjMrNvg&lib=MrFLn-5VfoXUA4xzR5GLj1UbMvH4Ype3g")
    Call<List<ApiResponseItem>> getData();
}
