# Task and Goal

Create an application or web service based on the LLM engine that analyzes business requirements, epics, corresponding user stories, and test cases. Provide a report to product owners, test managers, or other stakeholders about the quality and completeness of created epics, user stories, and test cases.

## Application Features

- Retrieves information from different sources:
  - Business requirements from doc files
  - Epics linked to Jira
  - User stories linked to epics
  - Tests linked to epics
- Utilizes business requirements and epic descriptions as initial demands
- Checks the quality and completeness of user stories and test cases associated with epics

## User Stories

- Highlights missing user stories and/or requirements in user stories
- Identifies redundant requirements in user stories
- Provides a draft of missing user stories
- Offers advice for improving existing user stories (e.g., according to quality standards)

## Test Cases

- Highlights missing test cases
- Identifies redundant test cases
- Shows estimated test coverage, including negative test cases
- Provides a draft of test cases to achieve 80% test coverage
- Offers advice for improving existing user stories (e.g., according to quality standards)

## Report View

- No specific requirements; the team decides the layout
- Quality standards for elements are not specified; the team decides

## Additional Information

- Application should count token numbers for each request to LLM and display the total number in the report.

## Datasets

Use two datasets from Mavi for testing the application and demonstration:

- **"Checkout" Business Requirements and Epic:** [MAVI-22883](https://jira.telekom.de/browse/MAVI-22883)
- **"Select Tariff" Business Requirements and Epic:** [MAVI-23400](https://jira.telekom.de/browse/MAVI-23400)

## Evaluation Criteria

- Must be workable and meet necessary requirements
- The result must be reproducible
- Aim for cost efficiency (fewer tokens)
- Implement a doc upload feature (not hardcoded)
- Evaluate the number of features the team managed to implement in the report
- Additional points for extra features, workarounds, or innovative technical solutions
