package tests.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.fakeapiuser.Address;
import models.fakeapiuser.Geolocation;
import models.fakeapiuser.Name;
import models.fakeapiuser.UserRoot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class SimpleApiTests {

    @Test
    public void getAllUsersTest() {
        given().get("https://fakestoreapi.com/users")
                .then()
                .log().all()
                .statusCode(200);

    }

    @Test
    public void getSingleUserTest(){
        int userId = 5;
        given().pathParam("userId", userId)
                .get("https://fakestoreapi.com/users/{userId}")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(userId))
                .body("address.zipcode", matchesPattern("\\d{5}-\\d{4}"));
    }

    @Test
    public void getAllUsersWithLimit(){
        int limitSize = 3;
        given().queryParam("limit", limitSize)
                .get("https://fakestoreapi.com/users")
                .then()
                .log().all()
                .statusCode(200)
                .body("", hasSize(limitSize));

    }

    @Test
    public void getAllUserSortByDescTest(){
        String sortType = "desc";
        Response sortedResponse = given().queryParam("sort", sortType)
                .get("https://fakestoreapi.com/users")
                .then()
                .log().all()
                .extract().response();

        Response notSortedResponse = given().get("https://fakestoreapi.com/users")
                .then().log().all()
                .extract().response();

        List<Integer> sortedResponseIds = sortedResponse.jsonPath().getList("id");
        List<Integer> notSortedResponseIds = notSortedResponse.jsonPath().getList("id");

        List<Integer> sortedByCode = notSortedResponseIds.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        Assertions.assertNotEquals(sortedResponseIds, notSortedResponseIds);
        Assertions.assertEquals(sortedResponseIds, sortedByCode);
    }

    @Test
    public void addNewUserTest(){
        Name name = new Name("aisen", "yaVor");
        Geolocation geolocation = new Geolocation("-35.2453", "81.8756");
        Address address = Address.builder()
                .city("Moscow")
                .street("Ul Pokrovka")
                .number(109)
                .geolocation(geolocation)
                .zipcode("34566-7689").build();

        UserRoot userRoot = UserRoot.builder()
                .email("aisen@gmail.com")
                .phone("798856378")
                .address(address)
                .username("aisen")
                .name(name)
                .password("Bars003").build();

        given().body(userRoot)
                .post("https://fakestoreapi.com/users")
                .then().log().all()
                .statusCode(200)
                .body("id", notNullValue());


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
    public void updateUserTest(){
        UserRoot user = getTestUser();
        String oldPassword = user.getPassword();

        user.setPassword("OAKLEY576");
        given().body(user)
                .put("https://fakestoreapi.com/users/11")
                .then().log().all()
                .statusCode(200)
                .body("password", not(equalTo(oldPassword)));
    }

    @Test
    public void deleteUserTest(){
        given().delete("https://fakestoreapi.com/users/7")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void authUserTest(){
        Map<String, String> userAuth = new HashMap<>();
        userAuth.put("username", "mor_2314");
        userAuth.put("password", "83r5^_");

        given().contentType(ContentType.JSON).body(userAuth)
                .post("https://fakestoreapi.com/auth/login")
                .then().log().all()
                .statusCode(200)
                .body("token", notNullValue());
    }


}
