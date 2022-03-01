var Funil = require('funil')

var createCollectorStream = require('./lib/collector-stream')
var createPathStream = require('./lib/path-stream')
var generators = require('./lib/gen-streams')
var dirStreams = require('./lib/dir-streams')
var createCheckDirStream = dirStreams.testDir
var createPrefixStream = dirStreams.prefixer
var exportStreams = require('./lib/export-streams')

module.exports = buster

function buster (options) {
  var pathStream = createPathStream()

  // / attach simple path stream

  var state = {
    main: false,
    prefix: 0,
    checkDir: !!options.depth
  }

  var listStream = generators.createListStream(options.list)
  listStream.pipe(pathStream, {end: false})
  listStream.on('end', function () {
    state.main = true
    if (!state.checkDir) {
      pathStream.end()
    }
  })

  listStream.resume()

  // / check for dirs

  if (options.depth) {
    var anotherListStream = generators.createListStream(options.list)
    var checkDirStream = createCheckDirStream(options.url, foundDir)
    checkDirStream.setMaxListeners(0)

    anotherListStream.on('end', function () {
      checkDirStream.on('drain', function () {
        if (state.main && state.prefix === 0) {
          pathStream.end()
        }
      })
    })

    anotherListStream.pipe(checkDirStream, {end: false})
    anotherListStream.resume()
  }

  function foundDir (dirPath) {
    var yetAnotherListStream = generators.createListStream(options.list)
    var prefixStream = createPrefixStream(dirPath)
    state.prefix += 1
    prefixStream.on('end', function () {
      state.prefix -= 1
    })

    yetAnotherListStream.pipe(prefixStream)
    prefixStream.pipe(checkDirStream, {end: false})
    prefixStream.pipe(pathStream, {end: false})

    yetAnotherListStream.resume()
  }

  // / attach the collectors

  var collectorsFunil = new Funil()
  collectorsFunil.setMaxListeners(0)

  var tfactor
  if (options.throttle) {
    tfactor = (options.throttle / options.methods.length) + 1
  }

  options.methods.forEach(function (method) {
    switch (method) {
      case 'HEAD': attachCollector('HEAD')
        break
      case 'GET': attachCollector('GET')
        break
      case 'POST': attachCollector('POST')
        break
      case 'PUT': attachCollector('PUT')
        break
      case 'DELETE': attachCollector('DELETE')
        break
    }
  })

  function attachCollector (method) {
    var collectorDel = createCollectorStream(options.url,
      method, tfactor, options.extension)
    collectorDel.setMaxListeners(0)
    pathStream.pipe(collectorDel)
    collectorsFunil.add(collectorDel)
  }

  // / pick here the right exportStream

  var exportStream

  switch (options.export) {
    case 'txt':
      break
    case 'xml':
      break
    case 'csv':
      break
    default: exportStream = exportStreams.createToJSON()
      break
  }

  collectorsFunil
    .pipe(exportStream)
    .pipe(options.outStream)

  pathStream.resume()
}
