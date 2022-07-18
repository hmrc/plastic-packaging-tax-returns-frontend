# Plastic Packaging Tax Returns (PPT)

This is the Scala microservice responsible for the transient storage of PPT returns information and PPT Account section,
which is part of the PPT tax regime, as discussed in this [GovUk Guidance](https://www.gov.uk/government/publications/introduction-of-plastic-packaging-tax/plastic-packaging-tax)

### How to run the service

These are the steps to the Plastic Packaging Tax Returns and Account service, of which this microservice is part of.

* Start a MongoDB instance

* Start the microservices

```
# Start the plastic packaging services and dependencies
sm --start PLASTIC_PACKAGING_TAX_ALL -f

# confirm all services are running
sm -s
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

### Accessibilities Test

The following command will only run the accessibilities test in a11y directory. 
Please note that to run the accessibilities tests you will need to install 
Node v12 or above
```
sbt a11y:test
```

### Precheck

Before submitting a commit or pushing code remotely, please run
```
./precheck.sh
```
This will execute unit and integration tests, check the Scala style and code coverage

### Scalastyle

Project contains `scalafmt` plugin.

Commands for code formatting:

```
sbt scalafmt        # format compile sources
sbt test:scalafmt   # format test sources
sbt sbt:scalafmt    # format .sbt source
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

