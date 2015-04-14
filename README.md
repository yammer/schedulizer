# Schedulizer

- Schedulizer is a powerful and ease to use schedule manager tool.
It was built to use a third app authentication.
It currently supports Facebook or Yammer integration.
<br>
You can create groups and add people to the groups. Then you create assignment types and assign people to days. Each user can specify in which days he is not available or partially available.
<br>

## Support
Schedulizer supports Chrome, Firefox, Safari, and IE >=9

## License

See [LICENSE.txt](LICENSE.txt)

## Running Schedulizer

You can choose to use IntelliJ or run from the terminal

### Using the command line

**(1)** Make sure you have [Java JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) installed in your machine

**(2)** Clone the schedulizer project

**(3)** Choose one of the authentication methods (either yammer or facebook)

**(4)** Go to the [app.yml](app.yml) located at the root of the project and set the `extApp` property to the app you chose. For example: `extApp: yammer`.

**(5)** Create your own app at [yammer](https://developer.yammer.com/v1.0/docs/getting-started) or [facebook](https://developers.facebook.com/quickstarts/?platform=web) to get an app id.

**(6)** Add your app id for schedulizer by setting the property `extAppClientId` at the [app.yml](app.yml) file. For example: `extAppClientId: YOUR_APP_CLIENT_ID`

**(7)** Make sure you don't have any other service running on the ports 8080 and 8081

**(8)** [Install maven](https://maven.apache.org/download.cgi)

**(9)** Run the server by running the script `run.sh`


### Using IntelliJ

**(1)** Make sure you have [Java JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) installed in your machine

**(2)** If you don't have yet, [download IntelliJ](https://www.jetbrains.com/idea/download/)

**(3)** Clone the schedulizer project

**(4)** Open the project in IntelliJ:
* Click on `Open`
* Choose the `pom.xml` file that is at the root folder of the project
* Click on `Open existing project`

**(5)** Add the program arguments in the run configuration:
* `Run > Edit Configurations`
* Add `server app.yml` at the `Program Arguments` box
* Click on `Apply`

**(6)** Choose one of the authentication methods (either yammer or facebook)

**(7)** Go to the [app.yml](app.yml) located at the root of the project and set the `extApp` property to the app you chose. For example: `extApp: yammer`.

**(8)** Create your own app at [yammer](https://developer.yammer.com/v1.0/docs/getting-started) or [facebook](https://developers.facebook.com/quickstarts/?platform=web) to get an app id.

**(9)** Add your app id for schedulizer by setting the property `extAppClientId` at the [app.yml](app.yml) file. For example: `extAppClientId: YOUR_APP_CLIENT_ID`


**(10)** Make sure you don't have any other service running on the ports 8080 and 8081


**(11)** Run the server
* Click on `Run > Run 'SchedulizerApplication'` or right click `src/main/java/com/yammer/schedulizer/SchedulizerApplication.java` and click on `Run 'SchedulizerApplication.main()'`

**(12)** If everything went fine, you should be able to see the website at [localhost:8080](http://localhost:8080)


## Contribution Guide

See [CONTRIBUTING.md](CONTRIBUTING.md)

## Authors

See [AUTHORS.txt](AUTHORS.txt)
