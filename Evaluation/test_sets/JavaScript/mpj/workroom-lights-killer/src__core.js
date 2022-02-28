module.exports = function core(services) {
  services.hue.nupnpSearch(function(error, result) {
    services.fs.readFile('/tmp/wlk-username.tmp', function(error, username) {
      var api;
      if (error) {
        api = new services.hue.HueApi()
        api.registerUser(result[0].ipaddress, null, 'My nice node api thingee',
          function(error, username) {
            if (error)
              services.console.log('Not authenticated. Please press the button on your bridge and run this script again.')
            else
              services.fs.writeFile('/tmp/wlk-username.tmp', username)
          })
      } else {
        api = new services.hue.HueApi(result[0].ipaddress, username)
        api.lights(function(error, result) {
          var lights = result.lights.filter(function(light) {
            return light.name.indexOf('Arbetsrummet') > -1
          })
          var offState = services.hue.lightState.create().off()
          lights.forEach(function(light) {
            api.setLightState(light.id, offState)
          })
          services.console.log('All workroom lights turned off')
        })
      }
    })

  })
}
