import groovy.json.JsonSlurper

println("Groovy processing payload '" + payload + "'");

def jsonSlurper = new JsonSlurper()
def message = jsonSlurper.parseText(payload)

if (message.routingKey != null) {
    return message.routingKey
}
else {
    return null
}