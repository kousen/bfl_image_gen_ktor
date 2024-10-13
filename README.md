## BFL Image Generator

This system accesses the RESTful web service provided by Black Forest Labs using their Flux 1.1 [pro] service. 
To use it, you need to register [there](https://docs.bfl.ml/quick_start/create_account/) and create a key. Save
that key as an environment variable called `BFL_API_KEY`. Images cost 4 cents each.

The implementation uses the `kotlinx.serialization` library and the [Ktor](https://ktor.io) Client library to
transmit the requests and receive the responses.

All images are saved with unique file names of the form `generated_<timestamp>.jpg` in the `src/main/resources`
directory.

Kotlin flows are uses to poll the service every half second to see when the images are ready.

Have fun,

Ken Kousen
