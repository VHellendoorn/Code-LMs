window.Mouse = window.Mouse || {};

(function($, Mouse) {

    var NUM_MOVES_TO_NOTIFY = 10,
        movements = 0,
		clientCount = 1,
        clients = {},
        unique_id = null,
        isMobile = 'ontouchstart' in document.documentElement,
        ws = null;

    /**
     * The initialization function which triggers the creation of your own
     * personal tracking dot as well as creates the WS connection and defines
     * the callbacks.
     */
    Mouse.init = function() {

        // check for websocket support
        if (!('WebSocket' in window)) {
            alert('Your browser does not support native WebSockets.');
            return;
        }

        // create unique snowflake
        Mouse.createDot();

        // init websocket in the private global scope
        ws = new WebSocket('ws://127.0.0.1:8090');

        console.log('WebSocket initialized:');
        console.log(ws);

        /**
         * Once a websocket connection has been established, we begin tracking
         * mouse movements and sending them to the server.
         */
        ws.onopen = function() {
            console.log('WebSocket opened:');
            console.log(ws);

            // track mouse movements
            $(document.body)
                .bind('mousemove', Mouse.movementTracker)
                .bind('touchmove', Mouse.movementTracker)
				.bind('click', Mouse.clickTracker);

			// try to trigger proper close event
			$(window).unload(Mouse.removeClient);
        };

        /**
         * The close event. We don't do anything here.
         */
        ws.onclose = function(e) {
			console.log('Close event triggered.');
			console.log(ws);
			console.log(e);
        };

        /**
         * The messages coming from the server formatted in JSON.
         */
        ws.onmessage = function(e) {
			// convert data to JSON
			try {

				var data = JSON.parse(e.data);

				// check for message type
				if (data.event == 'close') {
					Mouse.removeDot(data.id);
					return true;
				}

				// if the client just connected, create them a cached entry
				if (!clients[data.id]) {
					clients[data.id] = data;
					clientCount++;
				} else if (data.event == 'move') {
					// client already exists, update positioning only
					clients[data.id].xPos = data.xPos;
					clients[data.id].yPos = data.yPos;
				}

				// trigger movement tracking
				Mouse.moveDot(data.id, data.event);

			} catch (e) {
				console.log('An error occurred attempting to parse JSON response.');
				console.log(e);
			}
        };

        /**
         * Handle errors, i.e. do nothing.
         */
        ws.onerror = function(e) {
			console.log('An error occurred.');
			console.log(e);
        };

		/**
		 * Periodically checks for updates to the number of connected users.
		 */
		setInterval(function() {
			$('#notice').text(clientCount + ' connected users.');
		}, 250);
    };

	/**
	 * Watches for mouse clicks and broadcasts them to other clients as a
	 * "ping" event.
	 */
	Mouse.clickTracker = function(event) {
		var msg = { event: 'ping', id: unique_id };

		// trigger ping
		Mouse.moveDot(unique_id, 'ping');

		ws.send(JSON.stringify(msg));

		return false;
	};

    /**
     * Tracks mouse movements, only informing the server after every
     * NUM_MOVES_TO_NOTIFY movements.
     */
    Mouse.movementTracker = function(event) {
        var msg,
            touch;

        // increment movement counter
        movements++;

		// selectively send movements
        if (movements % NUM_MOVES_TO_NOTIFY == 0) {
			// update snowflake position
            if (isMobile) {
                touch = e.originalEvent.touches[0] || e.originalEvent.changedTouches[0];

                clients[unique_id].xPos = touch.pageX;
                clients[unique_id].yPos = touch.pageY;
            } else {
                clients[unique_id].xPos = event.pageX;
                clients[unique_id].yPos = event.pageY;
            }

			// actually track mouse
			Mouse.moveDot(unique_id, 'move');

            // create message to send to server
            msg = jQuery.extend({}, clients[unique_id]);
            msg.elem = null;
			msg.event = 'move';

			// stringify and send to server
			msg = JSON.stringify(msg);
            ws.send(msg);

            // reset movement count
            movements = 0;
        }
    };

    /**
     * Generates the user their own unique color and dot for tracking movements.
     */
    Mouse.createDot = function() {
		unique_id = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
			var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
			return v.toString(16);
		});

		clients[unique_id] = {
			id: unique_id,
			zIndex: Math.floor((Math.random() * 999999) + 1),
			radius: Math.floor((Math.random() * 10) + 5),
			color: (Math.random() * 0xFFFFFF + 1 << 0).toString(16),
			xPos: '50%',
			yPos: '50%'
		};

        while (clients[unique_id].color.length < 6) {
            clients[unique_id].color = "0" + clients[unique_id].color;
        }
        clients[unique_id].color = '#' + clients[unique_id].color;

		// display the dot
		Mouse.moveDot(unique_id);
    };

	/**
	 * Handles both the display and movement of client dots.
	 */
	Mouse.moveDot = function(client_id, event) {
		var snowflake = clients[client_id],
			css = {},
			pingFactor = 10,
			animSpeed = 10,
			width;

		// initial dot creation
		if (!clients[client_id].elem) {
			clients[client_id].elem = $('<div id="sf' + client_id + '" class="snowflake"></div>');

			css = {
				position: 'absolute',
				top: '50%',
				left: '50%',
				width: (snowflake.radius * 2) + 'px',
				height: (snowflake.radius * 2) + 'px',
				borderRadius: snowflake.radius + 'px',
				zIndex: snowflake.zIndex,
				background: snowflake.color
			};

			// temporarily update css
			clients[client_id].elem.css(css);

			// add to DOM
			clients[client_id].elem.appendTo('body');

			// return early
			return;
		}

		// check the event type
		if (event == 'move') {
			// a move event triggered, so we update positioning
			width = snowflake.radius * 2;

			css = {
				top: (clients[client_id].yPos - snowflake.radius) + 'px',
				left: (clients[client_id].xPos - snowflake.radius) + 'px',
				width: width + 'px',
				height: width + 'px',
				borderRadius: snowflake.radius + 'px',
				zIndex: snowflake.zIndex
			};
		} else if (event == 'ping') {
			// a ping event triggered, so we show a visual effect
			width = snowflake.radius * 2 * pingFactor;
			animSpeed = 50;

			css = {
				top: (clients[client_id].yPos - (snowflake.radius * pingFactor)) + 'px',
				left: (clients[client_id].xPos - (snowflake.radius * pingFactor)) + 'px',
				width: width + 'px',
				height: width + 'px',
				borderRadius: (snowflake.radius * pingFactor) + 'px',
				zIndex: snowflake.zIndex * 2
			};
		}

		// set css
		clients[client_id].elem.animate(css, animSpeed);
	};

	/**
	 * Handles removing dots from disconnected clients.
	 */
	Mouse.removeDot = function(client_id) {
		// only close if already exists
		if (typeof clients[client_id] != 'undefined' && clients[client_id]) {

			if (clients[client_id].elem) {
				clients[client_id].elem.remove();
			}

			clients[client_id] = null;
			clientCount--;
		}
	};

	/**
	 * Handles an attempt to tell other clients that the connection has closed.
	 */
	Mouse.removeClient = function() {
		var msg = {
			event: 'close',
			id: unique_id
		};

		ws.send(JSON.stringify(msg));
		return true;
	};

    // initialize
    Mouse.init();

})(jQuery, window.Mouse);
