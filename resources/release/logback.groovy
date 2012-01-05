import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender

import static ch.qos.logback.classic.Level.*

appender("stdout", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %msg%n" }}

appender("logfile", FileAppender) {
    file = "jlp.log" 
    encoder(PatternLayoutEncoder) {
        pattern = "%date %level %logger{10} [%file:%line] %msg%n" }}

root(WARN, ["stdout"])
//logger("com.jdblabs.jlp", TRACE, ["file"])
