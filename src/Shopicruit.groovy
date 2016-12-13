import groovy.json.JsonSlurper;
import groovy.json.JsonParserType;

def getPriceOfAll = {endpoint, productTypes ->
    parsePagedJsonFrom(endpoint)
    .findAll{ it.product_type in productTypes }*.variants*.price
    .flatten()
    .inject(0) { sum, item ->  sum + Double.valueOf(item) }
}

try {
    def money = getPriceOfAll(
        "http://shopicruit.myshopify.com/products.json",
        ["Watch","Clock"]
    )
    println java.text.NumberFormat.getCurrencyInstance().format(money);
} 
catch(Exception e) {
    prinln "An error occurred: " + e.getMessage()
}

def parsePagedJsonFrom(String endpoint) { 
    def parsePage = makeJsonPageParserFor endpoint
    parsePagedJson(parsePage, 1)
}

def parsePagedJson(Closure parsePage, int pageNum) {
    def products = parsePage(pageNum).products;
    if(products?.empty)
        []
    else
        products + parsePagedJson(parsePage, pageNum + 1)
}

def makeJsonPageParserFor(String baseUrl) {
    def slurper = new JsonSlurper().setType(JsonParserType.INDEX_OVERLAY)
    return { pageNum -> slurper.parse( "$baseUrl?page=$pageNum".toURL() ) }
}