import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.testng.Assert;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class PaytmUpcomingMovies {

	private Workbook workbook;

	@Test
	void getMovieDetails() throws IOException {

		System.out.println("***************** API Testing Started ******************");

		// Specify Base URI
		RestAssured.baseURI = "https://apiproxy.paytm.com/v2/movies/upcoming";

		// Request Object
		RequestSpecification PaytmHttpRequest = RestAssured.given();

		// Response Object
		Response PaytmResponse = PaytmHttpRequest.request(Method.GET);

		// Json path of Response
		JsonPath PaytmJson = PaytmResponse.jsonPath();

		// Print response in console
		String Responsebody = PaytmResponse.getBody().asString();
		System.out.println("Paytm Upcoming Movies : " + Responsebody);

		// 1. Verify Status Code
		checkStatusCode(PaytmResponse);

		// 2. Verify Release date
		checkReleaseDate(PaytmJson);

		// 3. Verify Poster URL
		checkPosterURL(PaytmJson);

		// 4. Verify Paytm Unique Movie Code
		checkMovieCode(PaytmJson);

		// 5. Print Movie Names in Excel File
		printMovieName(PaytmJson);

		System.out.println("***************** API Testing End ******************");
	}

	// 1. Status Code Verification
	void checkStatusCode(Response PaytmResponse) {

		int PaytmStatusCode = PaytmResponse.getStatusCode();
		System.out.println("Status Code of API is: " + PaytmStatusCode);
		AssertJUnit.assertEquals(200, PaytmStatusCode);
		System.out.println();
		System.out.println("Successfully Verified the Status Code of API to " + PaytmStatusCode);
		System.out.println();
	}

	// 2. Release Date Verification
	void checkReleaseDate(JsonPath PaytmJson) {

		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = formatter.format(date);
		// System.out.println("Current Date with Format yyyy-MM-dd : " + strDate);

		List<String> allReleaseDates = PaytmJson.getList("upcomingMovieData.releaseDate");

		// Iterate over the list and compare release date with current date
		for (String ReleaseDate : allReleaseDates) {
			if (ReleaseDate != null) {
				// System.out.println("Release Date is: " + ReleaseDate);

				try {
					Assert.assertTrue(formatter.parse(strDate).before(formatter.parse(ReleaseDate)),
							"The Release Date is: " + ReleaseDate + "  which is wrong");
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println();
		System.out.println("Successfully Verified the Release Dates of all upcoming movies");
		System.out.println();

	}

	// 3. Poster URL Verification
	void checkPosterURL(JsonPath PaytmJson) {

		List<String> allImages = PaytmJson.getList("upcomingMovieData.moviePosterUrl");

		// Iterate over the list and compare Poster URL
		for (String PosterURL : allImages) {
			if (PosterURL != null) {
				Assert.assertTrue(PosterURL.endsWith(".jpg"), "The Poster URL is: " + PosterURL + "  which is wrong");
			}
		}

		System.out.println();
		System.out.println("Successfully Verified the Poster URL of all upcoming movies");
		System.out.println();
	}

	// 4. Paytm Unique Movie Code Verification
	void checkMovieCode(JsonPath PaytmJson) {

		final Set<String> set1 = new HashSet<String>();

		List<String> allMovieCodes = PaytmJson.getList("upcomingMovieData.paytmMovieCode");

		// Iterate over the list and compare movie codes
		for (String MovieCode : allMovieCodes) {
			if (MovieCode != null) {
				Assert.assertTrue(set1.add(MovieCode), "The Movie Code is: " + MovieCode + "  which is not unique");
			}
		}

		System.out.println();
		System.out.println("Successfully Verified the Unique Paytm Movie Codes of all upcoming movies");
		System.out.println();
	}

//	// 5. Print Movie Name in Excel
	void printMovieName(JsonPath PaytmJson) throws IOException {

		workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet();

		int rowCount = 0;

		List<HashMap<String, Object>> allMovieData = PaytmJson.getList("upcomingMovieData");

		System.out.println("List of Movie Names whose Content Available is 0 :-");
		sheet.createRow(0).createCell(0).setCellValue("List of Movie Names whose Content Available is 0 :-");

		for (HashMap<String, Object> singleObject : allMovieData) {

			if (singleObject.get("isContentAvailable").equals(0)) {

				System.out.println(singleObject.get("provider_moviename"));

				String MovieName = (String) singleObject.get("provider_moviename");
				sheet.createRow(++rowCount).createCell(0).setCellValue(MovieName);
			}

		}

		try (FileOutputStream outputStream = new FileOutputStream("MovieNames.xls")) {
			workbook.write(outputStream);
		}

	}

}
