# Plastic Packaging Tax Returns (PPT)

This is the Scala microservice responsible for the transient storage of PPT returns information and PPT Account section,
which is part of the PPT tax regime, as discussed in this [GovUk Guidance](https://www.gov.uk/government/publications/introduction-of-plastic-packaging-tax/plastic-packaging-tax)

### How to run the service

These are the steps to the Plastic Packaging Tax Returns and Account service, of which this microservice is part of.

* Start a MongoDB instance

* Start the microservices

```
# Start the plastic packaging services and dependencies
sm2 --start PLASTIC_PACKAGING_TAX_ALL

# confirm all services are running
sm2 -s
```

* Visit http://localhost:9949/auth-login-stub/gg-sign-in
* You may need to add some user details to the form:
  * `Enrolment Key`: `HMRC-PPT-ORG`
  * `Identifier Name`: `EtmpRegistrationNumber`
  * `Identifier Value`: `XMPPT0000000001`
* Then enter a redirect url:
  * To start a return: http://localhost:8505/plastic-packaging-tax/submit-return-for-plastic-packaging-tax/submit-return 
  * To see account page: http://localhost:8505/plastic-packaging-tax/account 
* Press **Submit**.

### Accessibility Tests

The following command will only run the accessibility tests in a11y directory.
(You will need to install Node v12+ and npm.)
```
sbt a11y:test
```

### Pushing / merging code

Before pushing code for PR / merging, please run
```
sbt all
```
This runs the unit, integration, and a11y tests

### Scalastyle

We no longer use Scalastyle.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

