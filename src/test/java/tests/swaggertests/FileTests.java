package tests.swaggertests;

import assertions.Conditions;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import services.FileService;

import java.io.File;
import java.util.Random;

public class FileTests {
    private static FileService fileService;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://85.192.34.140:8080";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        fileService = new FileService();
    }

    @Test
    public void positiveDownLoadTest(){
        byte [] file = fileService.downLoadBaseImage()
                .asResponse().asByteArray();

        File expectedFile = new File("src/test/resources/threadqa.jpeg");

        Assertions.assertEquals(expectedFile.length(), file.length);
    }

    @Test
    public void positiveUploadTest(){
        File expectedFile = new File("src/test/resources/threadqa.jpeg");
        fileService.upLoadFile(expectedFile)
                .should(Conditions.hasStatusCode(200));

        byte [] actualFile = fileService.downLoadLastFile()
                .asResponse().asByteArray();
        Assertions.assertTrue(actualFile.length != 0);
        Assertions.assertEquals(expectedFile.length(), actualFile.length);
    }
}
