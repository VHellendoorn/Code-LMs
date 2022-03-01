
var url    = require('url'),
    _      = require('underscore');

/**
 * Non-semantic subdomain tokens to remove
 * @type {Array}
 */
var removableDomains = [
  'm.',
  'www.'
];

/**
 * Domain transformation map (for international domains)
 * @type {Object}
 */
var domainTransformationMap = {
  "amazon" : "amazon.com",
  "bing"   : "bing.com",
  "google" : "google.com"
};

var domainTransformations = _.keys(domainTransformationMap);

/**
 * Shortens domains by removing non-semantic subdomains (www, m) and
 * transforms common international domains (google.co.uk => google.com)
 * @param  {string} domain Domain
 * @return {string}        Shortened domain
 */
var shortenDomain = exports.domain = function (domain) {
  if (!domain) return '';

  _.each(removableDomains, function (removable) {
    if (domain.indexOf(removable) === 0)
      domain = domain.replace(removable, '');
  });

  // turn google.co.uk into google.com but leave news.google.co.uk to news.google.com
  var tokens = domain.split('.');
  for (var i = 0; i < tokens.length; i += 1) {
    var token = tokens[i];
    if (_.contains(domainTransformations, token)) {
      // we just matched "google" in the news.google.co.uk
      domain = domainTransformationMap[token];
      // add all the backward tokens
      // like the news. in the news.google.co.uk
      for (var j = i - 1; j >= 0; j -= 1) {
        domain = tokens[j] + '.' + domain;
      }

      break;
    }
  }

  return domain;
};


var shortenUrl = exports.url = function (u) {
  if (!u) return '';

  var parsed = url.parse(u);

  // if we don't have a domain, then just return it
  if (!parsed.host) return '';

  return  shortenDomain(parsed.host) + trim(parsed.pathname) +
          getShortHash(parsed.pathname, parsed.hash);
};

/**
 * Gets a short hash tag if there is no query string and the hash tag is short.
 * @param  {string} pathname Url pathname
 * @param  {string} hash     Url hash
 * @return {string}          Short hash if one is available, otherwise empty string
 */
var getShortHash = function (pathname, hash) {

  var result = '';
  if (!hash) return result;

  if ((_.isEmpty(pathname) || pathname.length < 2) &&
      (hash.length > 0 && hash.length < 20)) {

    result = '/' + trim(hash);
  }

  return result;
};

/**
 * Returns whether the string starts with the comparison string
 * @param  {string} str    Original string
 * @param  {string} suffic Comparison string
 * @return {boolean}    Whether the original string starts with the comparison string
 */
var endsWith = function (str, suffix) {
  return str.indexOf(suffix, str.length - suffix.length) !== -1;
};

/**
 * Trims all ending slashes and question marks from the end of the url
 * @param  {string} str String to trim
 * @return {string}     Trimmed string
 */
var trim = function (str) {
  while(endsWith(str, '/')  || endsWith(str, '?')) {
    str = str.substring(0, str.length - 1);
  }
  return str;
};