package xyz.rk0.stats.housing.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Optional;

import com.google.gson.JsonParser;
import io.cucumber.java.Before;
import org.junit.Assert;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class StepDefinitions {

	private String stat;
	private String field;
	private Optional<String> zipCode;
	private Optional<String> startDate;
	private Optional<LocalDate> endDate;
	private Double statValue;
	private String responseError;

	@Before
	public void setup() {
		this.zipCode = Optional.empty();
		this.startDate = Optional.empty();
		this.endDate = Optional.empty();
		this.statValue = null;
		this.responseError = null;
	}

	@Given("I am looking for (the ){string} (of the ){string}")
	public void setupMainFields(String stat, String field) {
		this.stat = stat;
		this.field = field;
	}
	
	@Given("I have no filters")
	public void withNoFilters() {
		this.zipCode = Optional.empty();
		this.startDate = Optional.empty();
		this.endDate = Optional.empty();
	}

	@Given("I am filtering zip code to {string}")
	public void withZipCodeFilter(String zipCode) {
		this.zipCode = Optional.of(zipCode);
	}

	@Given("I am filtering start date to {string}")
	public void iAmFilteringStartDateTo(String startDate) {
		this.startDate = Optional.of(startDate);
	}
	
	@When("I request the statistics endpoint")
	public void makeRequest() throws URISyntaxException, IOException, InterruptedException {
		var client = HttpClient.newHttpClient();
		var request = HttpRequest.newBuilder(
			new URI("http://localhost:8080/housing-stats?"
					+ "statistic=" + this.stat
					+ "&field=" + this.field
					+ this.startDate.map(startDate -> "&startDate=" + startDate).orElse("")
					+ this.endDate.map(endDate -> "&endDate=" + endDate).orElse("")
					+ this.zipCode.map(zip -> "&zipCode=" + zip).orElse("")
			)
		).build();
		var resp = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (resp.statusCode() == 200) {
			this.statValue = JsonParser.parseString(resp.body())
				.getAsJsonObject()
				.get("value")
				.getAsDouble();
		} else {
			this.responseError = JsonParser.parseString(resp.body())
				.getAsJsonObject()
				.get("error")
				.getAsString();
		}
	}

	@Then("I expect a value of {double}")
	public void testValue(Double expected) {
		Assert.assertEquals(expected, Math.round(this.statValue * 100) / 100.0, 0.000001);
	}

	@Then("I will see an error message with {string}")
	public void testError(String expectedError) {
		Assert.assertEquals(expectedError.toLowerCase(), this.responseError.toLowerCase());
	}

	@Then("I should see some kind of error")
	public void iShouldSeeSomeKindOfError() {
		Assert.assertTrue(this.responseError.length() > 0);
	}
}
