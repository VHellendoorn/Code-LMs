/**
 * @fileoverview Example reactive SiteQuery
 * @author roger.castillo@loku.com (Roger Castillo)
 */

var SiteQuery = require('../lib/sitequery').SiteQuery;

// create a new SiteQuery of depth 2 with a delay of 1s between next page crawl
// selecting for `img` elements on each page
// Note: Webcrawling is delayed and will not be executed
// until Subscription

var crawlOpts = {url:'http://loku.com', maxDepth:2, delay:1000, maxCrawlTime: 100000}
var siteQuery = new SiteQuery(crawlOpts, 'img');

// ask for the observable sequence and subscribe for selected jQuery element(s)
siteQuery.toObservable().Subscribe(function(result) {
// output the img src
  console.log(result.sourceUrl, result.elem.attr('src'));
},
// on err
function(exn) {
  console.log('Something blowd up with exception:' + exn);
},
// on crawl complete
function() {
  console.log('SiteQuery complete');
  process.exit(0);
});