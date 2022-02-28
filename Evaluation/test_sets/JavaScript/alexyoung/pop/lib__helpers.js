/*!
 * Pop - HTML processing helpers.
 * Copyright 2011 Alex R. Young
 * MIT Licensed
 */

/**
 * Module dependencies and local variables.
 */
var jade = require('jade')
  , fs = require('fs')
  , path = require('path')
  , cache = {}
  , _date = require('underscore.date')
  , helpers;

helpers = {
  /**
   * Renders a Jade template
   *
   * @param {String} A Jade template
   * @param {Object} Options passed to Jade
   * @return {String}
   */
  render: function(template, options) {
    options = options || {};
    options.pretty = true;
    var fn = jade.compile(template, options);
    return fn(options.locals);
  },

  /**
   * Pagination links.
   *
   * @param {Object} Paginator object
   * @return {String}
   */
  paginate: function(paginator) {
    var template = '';
    template += '.pages\n';
    template += '  - if (paginator.previousPage)\n';
    template += '    span.prev_next\n';
    template += '      - if (paginator.previousPage === 1)\n';
    template += '        span &larr;\n';
    template += '        a.previous(href="/") Previous\n';
    template += '      - else\n';
    template += '        span &larr;\n';
    template += '        a.previous(href="/page" + paginator.previousPage + "/") Previous\n';
    template += '  - if (paginator.pages > 1)\n';
    template += '    span.prev_next\n';
    template += '      - for (var i = 1; i <= paginator.pages; i++)\n';
    template += '        - if (i === paginator.page)\n';
    template += '          strong.page #{i}\n';
    template += '        - else if (i !== 1)\n';
    template += '          a.page(href="/page" + i + "/") #{i}\n';
    template += '        - else\n';
    template += '          a.page(href="/") 1\n';
    template += '      - if (paginator.nextPage <= paginator.pages)\n';
    template += '        a.next(href="/page" + paginator.nextPage + "/") Next\n';
    template += '        span &rarr;\n';
    return helpers.render(template, { locals: { paginator: paginator } });
  },

  /**
   * Generates paginated blog posts, suitable for use on an index page.
   *
   * @return {String}
   */
  paginatedPosts: function() {
    var template
      , site = this;

    template = ''
      + '- for (var i = 0; i < paginator.items.length; i++)\n'
      + '  !{hNews(paginator.items[i], true)}\n';
    return helpers.render(template, { locals: site.applyHelpers({ paginator: site.paginator }) });
  },

  /**
   * Atom Jade template.
   *
   * @param {String} Feed URL
   * @param {Integer} Number of paragraphs to summarise
   *
   * @return {String}
   */
  atom: function(feed, summarise) {
    var template = ''
      , url = this.config.url
      , title = this.config.title
      , perPage = this.config.perPage
      , posts = this.posts.slice(0, perPage)
      , site = this;

    summarise = typeof summarise === 'boolean' && summarise ? 3 : summarise;
    perPage = site.posts.length < perPage ? site.posts.length : perPage;

    template += '!!!xml\n';
    template += 'feed(xmlns="http://www.w3.org/2005/Atom")\n';
    template += '  title #{title}\n';
    template += '  link(href=feed, rel="self")\n';
    template += '  link(href=url)\n';

    if (posts.length > 0)
      template += '  updated #{dx(posts[0].date)}\n';

    template += '  id #{url}\n';
    template += '  author\n';
    template += '    name #{title}\n';
    template += '  - for (var i = 0, post = posts[i]; i < ' + perPage + '; i++, post = posts[i])\n';
    template += '    entry\n';
    template += '      title #{post.title}\n';
    template += '      link(href=url + post.url)\n';
    template += '      updated #{dx(post.date)}\n';
    template += '      id #{url.replace(/\\/$/, "")}#{post.url}\n';

    if (summarise)
      template += '      content(type="html") !{h(truncateParagraphs(post.content, summarise, ""))}\n';
    else
      template += '      content(type="html") !{h(post.content)}\n';

    return helpers.render(template, { locals: site.applyHelpers({
        paginator: site.paginator
      , posts: posts
      , title: title
      , url: url
      , feed: feed
      , summarise: summarise
    })});
  },

  /**
   * RSS Jade template.
   *
   * @param {String} Feed URL
   * @param {Integer} Number of paragraphs to summarise
   * @param {String} Optional description
   *
   * @return {String}
   */
  rss: function(feed, summarise, description) {
    var template = ''
      , url = this.config.url
      , title = this.config.title
      , perPage = this.config.perPage
      , posts = this.posts.slice(0, perPage)
      , site = this;

    description = description || title;
    summarise = typeof summarise === 'boolean' && summarise ? 3 : summarise;
    perPage = site.posts.length < perPage ? site.posts.length : perPage;

    template += '!!!xml\n';
    template += 'rss(version="2.0")\n';
    template += '  channel\n';
    template += '    title #{title}\n';
    template += '    link #{url}\n';
    template += '    description #{description}\n';

    // TODO: Site description, language, managingEditor, webMaster

    if (posts.length > 0) {
      template += '    pubDate #{d822(posts[0].date)}\n';
      template += '    lastBuildDate #{d822(posts[0].date)}\n';
    }

    template += '    generator Pop\n';
    template += '    - for (var i = 0, post = posts[i]; i < ' + perPage + '; i++, post = posts[i])\n';
    template += '      item\n';
    template += '        title #{post.title}\n';
    template += '        link #{url + post.url}\n';
    template += '        pubDate #{d822(post.date)}\n';
    template += '        guid #{url.replace(/\\/$/, "")}#{post.url}\n';

    if (summarise)
      template += '        description !{h(truncateParagraphs(post.content, summarise, ""))}\n';
    else
      template += '        description !{h(post.content)}\n';

    return helpers.render(template, { locals: site.applyHelpers({
        paginator: site.paginator
      , posts: posts
      , title: title
      , url: url
      , feed: feed
      , description: description
      , summarise: summarise
    })});
  },

  /**
   * Returns unique sorted tags for every post.
   *
   * @return {Array}
   */
  allTags: function() {
    var allTags = [];
    
    for (var key in this.posts) {
      if (this.posts[key].tags) {
        for (var i = 0; i < this.posts[key].tags.length; i++) {
          var tag = this.posts[key].tags[i];
          if (allTags.indexOf(tag) === -1) allTags.push(tag);
        }
      }
    }

    allTags.sort(function(a, b) {
      a = a.toLowerCase();
      b = b.toLowerCase();
      if (a < b) return -1;
      if (a > b) return 1;
      return 0;
    });

    return allTags;
  },

  /**
   * Get a set of posts for a tag.
   *
   * @param {String} Tag name
   * @return {Array}
   */
  postsForTag: function(tag) {
    var posts = [];
    for (var key in this.posts) {
      if (this.posts[key].tags && this.posts[key].tags.indexOf(tag) !== -1) {
        posts.push(this.posts[key]);
      }
    }
    return posts;
  },

  /**
   * Display a list of tags.
   *
   * TODO: Link options
   * 
   * @param {Array} Tag names
   * @return {String}
   */
  tags: function(tags) {
    return tags.map(function(tag) {
      return '<a href="/tags.html#' + escape(tag) + '">' + tag + '</a>';
    }).join(', ');
  },

  /**
   * Renders a post using the hNews microformat, based on:
   * 
   * http://www.readability.com/publishers/guidelines/#view-exampleGuidelines
   *
   * @param {Object} A post object
   * @return {String} Post in the hNews format
   */
  hNews: function(post, summary) {
    var template = '';
    template += 'article.hentry\n';
    template += '  header\n';
    template += '    h1.entry-title\n';
    template += '      a(href=post.url) !{post.title}\n';
    template += '    time.updated(datetime=dx(post.date), pubdate) #{ds(post.date)}\n';
    if (post.author)
      template += '    p.byline.author.vcard by <span class="fn">#{post.author}</span>\n';

    if (post.tags) template += '    div.tags !{tags(post.tags)}\n';

    if (summary) {
      if (post.summary) {
        template += '  !{post.summary + "<p><a class=\\"read-more\\" href=\\"' + post.url + '\\">Read More &rarr;</a></p>"}\n';
      } else {
        template += '  !{truncateParagraphs(post.content, 3, "<p><a class=\\"read-more\\" href=\\"' + post.url + '\\">Read More &rarr;</a></p>")}\n';
      }
    } else {
      template += '  !{post.content}\n';
    }
    return helpers.render(template, { locals: this.applyHelpers({ post: post }) });
  },

  /**
   * Formats a date with date formatting rules according to underscore.date's rules.
   * 
   * @param {Date} Date to format
   * @param {String} Date format
   * @return {String}
   */
  df: function(date, format) {
    return _date(date).format(format);
  },

  /**
   * Short date (01 January 2001).
   * 
   * @param {Date} Date to format
   * @return {String}
   */
  ds: function(date) {
    return helpers.df(date, 'DD MMMM YYYY');
  },

  /**
   * Atom date formatting.
   * 
   * @param {Date} Date to format
   * @return {String}
   */
  dx: function(date) {
    return helpers.df(date, 'YYYY-MM-DDTHH:MM:ssZ');
  },

  /**
   * RFC-822 dates.
   *
   * @param {Date} Date to format
   * @return {String}
   */
  d822: function(date) {
    // FIXME: why is _date always setting the timezone to local?
    return helpers.df(date, 'ddd, DD MMM YYYY HH:MM:ss') + ' GMT';
  },

  /**
   * Escapes brackets and ampersands.
   * 
   * @param {String} Text to escape
   * @return {String}
   */
  h: function(text) {
    return text && text.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
  },

  /**
   * Truncates HTML based on paragraph counts.
   * 
   * @param {String} Text to truncate
   * @param {Integer} Number of paragraphs
   * @param {String} Text to append when truncated
   * @return {String}
   */
  truncateParagraphs: function(text, length, moreText) {
    var t = text.split('</p>');
    return t.length < length ? text : t.slice(0, length).join('</p>') + '</p>' + moreText;
  },

  /**
   * Truncates based on characters (not HTML safe, use with pre-formatted text).
   * 
   * @param {String} Text to truncate
   * @param {Integer} Length
   * @param {String} Text to append when truncated
   * @return {String}
   */
  truncate: function(text, length, moreText) {
    return text.length > length ? text.slice(0, length).trim() + moreText : text;
  },

  /**
   * Truncates based on words (not HTML safe, use with pre-formatted text).
   * 
   * @param {String} Text to truncate
   * @param {Integer} Number of words
   * @param {String} Text to append when truncated
   * @return {String}
   */
  truncateWords: function(text, length, moreText) {
    var t = text.split(/\s/);
    return t.length > length ? t.slice(0, length).join(' ') + moreText : text;
  }
};

module.exports = helpers;
