Feature: Housing statistic server
  Tests housing statistics endpoint

  Scenario: Average sale price with no filter
    Given I am looking for "average" "price"
      And I have no filters
    When I request the statistics endpoint
    Then I expect a value of 234144.26

  Scenario: Min Square footage by zip code
    Given I am looking for the "max" "squareFootage"
      And I am filtering zip code to "95838"
    When I request the statistics endpoint
    Then I expect a value of 2142.0

  Scenario: Error handling for invalid statistic
    Given I am looking for the "standardDeviation" of the "price"
      And I have no filters
    When I request the statistics endpoint
    Then I will see an error message with "Invalid statistic: standardDeviation"

  Scenario: General error handling
    Given I am looking for the "max" of the "squareFootage"
      But I am filtering start date to "invalidDate"
    When I request the statistics endpoint
    Then I should see some kind of error