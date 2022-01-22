package org.betterdevxp.gradle.testkit

class TestFile extends File {

    TestFile(File file) {
        super(file.toURI())
    }

    TestFile(String pathname) {
        super(pathname)
    }

    TestFile(File parent, String child) {
        super(parent, child)
    }

    TestFile getParentFile() {
        super.parentFile != null ? new TestFile(super.parentFile) : null
    }

    TestFile file(String relativePath) {
        File file = new File(this, relativePath)
        new TestFile(file)
    }

    TestFile file(String relativePath, String fileName) {
        File parentDir = file(relativePath)
        new TestFile(new File(parentDir, fileName))
    }

    void write(String text) {
        parentFile.mkdirs()
         super.write(text)
    }

    TestFile leftShift(Object content) {
        parentFile.mkdirs()
        super.leftShift(content)
        this
    }

}
