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
    def parsePage = makeJsonPageParserFor(endpoint)
    parsePagedJson(parsePage, 1)
}

def parsePagedJson(Closure parsePage, int pageNum) {
    def products = parsePage(pageNum).products;
    if(products?.empty)
        []
    else
        products + parsePagedJson(parsePage, pageNum + 1)
}

/* Returns a closure with one parameter: the page to parse 
 * There is a better overload for parsing from URLs but it seems to be buggy at the time of writing */
def makeJsonPageParserFor(String baseUrl) {
    { pageNum -> new groovy.json.JsonSlurper().parseText new URL("$baseUrl?page=$pageNum").text }
}
