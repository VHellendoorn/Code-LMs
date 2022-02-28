var dirBuster = require('../src')
var Writable = require('stream').Writable

var options = {
  list: './simple-test.txt',
  outStream: new Writable({
    decodeStrings: false,
    objMode: false
  }),
  url: 'https://liftsecurity.io',
  export: 'json',
  methods: ['GET', 'POST'],
  depth: 2,
  throttle: 5
// extension: ['.php']
}

options.outStream.on('error', function (err) {
  console.log('err: ', err)
})

options.outStream._write = function (chunk, enc, next) {
  console.log(chunk.toString('utf8'))
  next()
}

options.outStream.on('finish', function () {
  console.log('ended')
  process.exit(1)
})

dirBuster(options)
