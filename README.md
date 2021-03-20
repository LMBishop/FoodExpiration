<p align="center">
<h1 align="center">FoodExpiration</h1>
</p>

## About
Spigot Plugin which adds expiration to food in Minecraft using new [PersistentDataContainers](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/persistence/PersistentDataContainer.html).

## Downloads/Building
Compiled versions of stable releases can be found on [SpigotMC](https://www.spigotmc.org/resources//)

Alternatively, you can build FoodExpiration yourself via Gradle from source using ``gradlew build``.

You can include FoodExpiration in your project using [JitPack](https://jitpack.io/#LMBishop/FoodExpiration):
### Maven
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<!-- FoodExpiration -->
<dependency>
    <groupId>com.github.LMBishop</groupId>
    <artifactId>FoodExpiration</artifactId>
    <version>master-SNAPSHOT</version>
<dependency>
```

### Gradle
```groovy
repositories {
    maven { url = 'https://jitpack.io' }
}  
dependencies {
    // FoodExpiration
    compileOnly 'com.github.LMBishop:FoodExpiration:master-SNAPSHOT'
}
```

## Contributors
See https://github.com/LMBishop/FoodExpiration/graphs/contributors

## License
The **source code** for FoodExpiration is licensed under the **GNU General Public License v3.0**. To view the full text of the license, click [here](https://github.com/LMBishop/FoodExpiration/blob/master/LICENSE.txt).

## Contributing
We welcome all contributions, we will check out all pull requests and determine if it should be added to FoodExpiration. 

Assistance of all forms is appreciated 🙂

### Guidance
* ensure Java 8 is installed on your machine (release versions are compiled against Java 8)
* fork this repository and clone it
* edit the source code as your please
* run ``gradlew build`` in the base directory to build FoodExpiration
* push to your fork when ready & submit a pull request

### Contribution Guidelines
If you plan on contributing upstream please note the following:
* discuss large changes first
* indent the file with **4 spaces**
* take a look at how the rest of the project is formatted and follow that
* do not alter the version number in ``build.gradle``, that will be done when the release version is ready
* limit the first line of commit messages to ~50 chars and leave a space below that
* **test your changes** on the latest Spigot version before making a pull request

By contributing to FoodExpiration you agree to license your code under the [GNU General Public License v3.0](https://github.com/LMBishop/FoodExpiration/blob/master/LICENSE.txt).
