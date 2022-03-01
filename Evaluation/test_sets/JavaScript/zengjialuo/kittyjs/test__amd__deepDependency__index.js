define( 
    'amd/deepDependency/index', 
    [ 
        'amd/deepDependency/level1', 
        'amd/deepDependency/level11'
    ], 
    function ( level1, level11 ) {
        return {
            name: 'amd/deepDependency/index',
            check: function () {
                var valid = 
                    level1.name == 'amd/deepDependency/level1'
                    && level11.name == 'amd/deepDependency/level11'
                    && level1.check()
                    && level11.check();
                return valid;
            }
        };
    }
);