package com.udayasreesoft.adminbusinessanalysis.retorfit

import com.udayasreesoft.mybusinessanalysis.retorfit.model.ZipcodeModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url

interface ApiInterface {
    @Headers("Cache-control: no-cache")
    @GET()
    fun getZipCodeAddress(@Url url: String) : Call<ZipcodeModel>
}