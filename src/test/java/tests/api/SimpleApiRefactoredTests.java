package tests.api;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.fakeapiuser.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class SimpleApiRefactoredTests {

    @BeforeAll
    public static void setUp(){
        RestAssured.baseURI = "https://fakestoreapi.com";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    private UserRoot getTestUser(){
        Name name = new Name("aisen", "yaVor");
        Geolocation geolocation = new Geolocation("-35.2453", "81.8756");
        Address address = Address.builder()
                .city("Moscow")
                .street("Ul Pokrovka")
                .number(109)
                .geolocation(geolocation)
                .zipcode("34566-7689").build();

        return UserRoot.builder()
                .email("aisen@gmail.com")
                .phone("798856378")
                .address(address)
                .username("aisen")
                .name(name)
                .password("Bars003").build();
    }

    @Test
    public void getAllUsersTest() {
        given().get("/users")
                .then()
                .statusCode(200);

    }

    @Test
    public void getSingleUserTest(){
        int userId = 5;
         UserRoot response = given().pathParam("userId", userId)
                .get("/users/{userId}")
                .then()
                .statusCode(200)
                .extract().as(UserRoot.class);

        Assertions.assertEquals(response.getId(), userId);
        Assertions.assertTrue(response.getAddress().getZipcode().matches("\\d{5}-\\d{4}"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    public void getAllUsersWithLimit(int limitSize){

        List<UserRoot> users = given().queryParam("limit", limitSize)
                .get("/users")
                .then()
                .statusCode(200)
                .extract().as(new TypeRef<List<UserRoot>>() {});

        Assertions.assertEquals(limitSize, users.size());

    }

    @ParameterizedTest
    @ValueSource(ints = {0, 40})
    public void getAllUsersWithLimitErrorParam(int limitSize){
        List<UserRoot> users = given().queryParam("limit", limitSize)
                .get("/users")
                .then()
                .statusCode(200)
                .extract().as(new TypeRef<List<UserRoot>>() {});

        Assertions.assertNotEquals(limitSize, users.size());
    }



    @Test
    public void getAllUserSortByDescTest(){
        String sortType = "desc";
        List<UserRoot> sortedResponse = given().queryParam("sort", sortType)
                .get("/users")
                .then()
                .extract().as(new TypeRef<List<UserRoot>>() {});

        List<UserRoot> notSortedResponse = given().get("/users")
                .then()
                .extract().as(new TypeRef<List<UserRoot>>() {});

        List<Integer> sortedResponseIds = sortedResponse.stream().map(x->x.getId()).collect(Collectors.toList());
        List<Integer> notSortedResponseIds = notSortedResponse.stream().map(x->x.getId()).collect(Collectors.toList());

        List<Integer> sortedByCode = notSortedResponseIds.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        Assertions.assertNotEquals(sortedResponseIds, notSortedResponseIds);
        Assertions.assertEquals(sortedResponseIds, sortedByCode);
    }

    @Test
    public void addNewUserTest(){
        UserRoot userRoot = getTestUser();

        int userId = given().body(userRoot)
                .post("/users")
                .then()
                .statusCode(200)
                .extract().jsonPath().getInt("id");

        Assertions.assertNotNull(userId);
    }

    @Test
    public void updateUserTest(){
        UserRoot user = getTestUser();
        String oldPassword = user.getPassword();

        user.setPassword("OAKLEY576");
        UserRoot updatedUser = given().body(user)
                .put("/users/11")
                .then()
                .extract().as(UserRoot.class);

        Assertions.assertNotEquals(oldPassword, updatedUser.getPassword());
    }

    @Test
    public void deleteUserTest(){
        given().delete("/users/7")
                .then()
                .statusCode(200);
    }

    @Test
    public void authUserTest(){
        AuthData authData = new AuthData("mor_2314","83r5^_");

        String token = given().contentType(ContentType.JSON).body(authData)
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);
    }

}
