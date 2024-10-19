package edu.trincoll

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val inputFile = File("src/main/resources/pirate_dogs/generated_image_20241008160655.jpg")
    val outputFile = File("src/main/resources/resized_image.jpg")

    val targetWidth = 1024
    val targetHeight = 576

    try {
        val inputImage: BufferedImage = ImageIO.read(inputFile)
        val outputImage = inputImage.resize(targetWidth, targetHeight)

        ImageIO.write(outputImage, "jpg", outputFile)  // Change "jpg" to the format you want (e.g., "png")
        println("Image resized successfully!")
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}

// Extension function to resize the image
fun BufferedImage.resize(targetWidth: Int, targetHeight: Int): BufferedImage {
    val resizedImage = this.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH)

    // Create the new buffered image with target size
    val outputImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)

    // Draw the resized image onto the new BufferedImage
    val g2d = outputImage.createGraphics() // g2d is of type Graphics2D
    g2d.drawImage(resizedImage, 0, 0, null)
    g2d.dispose()  // Explicitly dispose of the Graphics2D object

    return outputImage
}
