package com.example.hprestaurant.api;

import com.example.hprestaurant.Models.Account;
import com.example.hprestaurant.Models.Customer;
import com.example.hprestaurant.Models.LocationModel;
import com.example.hprestaurant.Models.MessageModel;
import com.example.hprestaurant.Models.Request.BookTableRequest;
import com.example.hprestaurant.Models.Request.CustomerRequest;
import com.example.hprestaurant.Models.Request.DateTimeRequest;
import com.example.hprestaurant.Models.Request.LoginRequest;
import com.example.hprestaurant.Models.Request.OrderRequest;
import com.example.hprestaurant.Models.Request.PasswordRequest;
import com.example.hprestaurant.Models.Request.ReservationRequest;
import com.example.hprestaurant.Models.Request.ServerRequest;
import com.example.hprestaurant.Models.ReservationModel;
import com.example.hprestaurant.Models.Response.LoginResponse;
import com.example.hprestaurant.Models.Response.ServerResponse;
import com.example.hprestaurant.Models.TableModel;
import com.example.hprestaurant.Utils.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    ApiService apiService = new Retrofit.Builder()
            .baseUrl("http://"+ Util.HOST+":3000")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService.class);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("account/check_password")
    Call<ServerResponse> passwordChecking(@Body PasswordRequest passwordRequest);

    @POST("account/create_password")
    Call<ServerResponse> createPassword(@Body PasswordRequest passwordRequest);

    @POST("account/add_new_account")
    Call<ServerResponse> addNewAccount(@Body Account account);

    @POST("user/phone_number_is_exists")
    Call<ServerResponse> checkPhoneNumberRequest(@Body LoginRequest request);

    @POST("user/add_new_customer")
    Call<ServerResponse> addNewCustomer(@Body Customer customer);

    @POST("user/get_info_customer")
    Call<Customer> getInfoCustomer(@Body ServerRequest request);

    @POST("user/update_customer")
    Call<Customer> updateCustomer(@Body Customer customer);

    @GET("home/get_all_locations")
    Call<ArrayList<LocationModel>> getAllLocations();

    @POST("home/get_conservation")
    Call<ArrayList<MessageModel>> getMessages(@Body ServerRequest request);

    @POST("booking/get_table_booking_by_day")
    Call<ArrayList<TableModel>> getAllTables(@Body DateTimeRequest request);

    @GET("booking/get_tables")
    Call<ArrayList<TableModel>> getTables();

    @GET("booking/get_reservations")
    Call<ArrayList<ReservationModel>> getReservations();

    @POST("booking/get_reservation_by_day")
    Call<ArrayList<ReservationModel>> getReservationByDay(@Body ServerRequest request);

    @POST("booking/reservation_in_progress")
    Call<ReservationModel> reservationInProgress(@Body BookTableRequest request);

    @POST("booking/reservation_in_progress_release")
    Call<ServerResponse> reservationInProgressRelease(@Body ReservationRequest request);

    @POST("booking/post_booking_table")
    Call<ServerResponse> booingTable(@Body BookTableRequest request);

    @POST("booking/dishes_only")
    Call<ServerResponse> bookingDishesOnly(@Body OrderRequest request);

    @POST("booking/dishes_and_table")
    Call<ServerResponse> bookingDishesAndTable(@Body OrderRequest request);


    @POST("reservation/get_reservation_by_customer_id")
    Call<ArrayList<ReservationModel>> getReservationByCustomerId(@Body CustomerRequest customerRequest);
}
