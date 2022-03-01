var onRun = function(context) {

    //Import utils.js
    @import "utils.js"

    function showAlertWindow() {
        var alertWindow = COSAlertWindow.new()

        // Set the icon fot the view
        alertWindow.setIcon(NSImage.alloc().initByReferencingFile(context.plugin.urlForResourceNamed("create-rectangle.png").path()));

        alertWindow.setMessageText('Create new rectangle')

        // Create the main view that contain the filed
        var mainView = NSView.alloc().initWithFrame(NSMakeRect(0, 0, 360, 180));
        alertWindow.addAccessoryView(mainView);

        // Add labels
        var widthLabel = createLabel(NSMakeRect(0, 160, 140, 20), "Width");
        mainView.addSubview(widthLabel);

        var heightLabel = createLabel(NSMakeRect(160, 160, 140, 20), "Height");
        mainView.addSubview(heightLabel);

        var xLabel = createLabel(NSMakeRect(0, 100, 140, 20), "X");
        mainView.addSubview(xLabel);

        var yLabel = createLabel(NSMakeRect(160, 100, 140, 20), "Y");
        mainView.addSubview(yLabel);

        var bgLabel = createLabel(NSMakeRect(0, 40, 140, 20), "Background");
        mainView.addSubview(bgLabel);

        // Add inputs
        var widthInput = NSTextField.alloc().initWithFrame(NSMakeRect(0, 140, 140, 20));
        widthInput.setStringValue("100");
        mainView.addSubview(widthInput);

        var heightInput = NSTextField.alloc().initWithFrame(NSMakeRect(160, 140, 140, 20));
        heightInput.setStringValue("100");
        mainView.addSubview(heightInput);

        var xInput = NSTextField.alloc().initWithFrame(NSMakeRect(0, 80, 140, 20));
        xInput.setStringValue("0");
        mainView.addSubview(xInput);

        var yInput = NSTextField.alloc().initWithFrame(NSMakeRect(160, 80, 140, 20));
        yInput.setStringValue("0");
        mainView.addSubview(yInput);

        var bgInput = NSTextField.alloc().initWithFrame(NSMakeRect(0, 20, 300, 20));
        bgInput.setStringValue("cccccc");
        mainView.addSubview(bgInput);

        // Add buttons to confirm or cancel
        alertWindow.addButtonWithTitle('OK')
        alertWindow.addButtonWithTitle('Cancel')

        // Allow tab to switch between inputs
        alertWindow.alert().window().setInitialFirstResponder(widthInput);
        widthInput.setNextKeyView(heightInput)
        heightInput.setNextKeyView(xInput)
        xInput.setNextKeyView(yInput)
        yInput.setNextKeyView(bgInput)
        bgInput.setNextKeyView(widthInput)

        //If "OK" is clicked
        if (alertWindow.runModal() == "1000") {

            // Create variable with user's input
            var width = widthInput.stringValue();
            var height = heightInput.stringValue();
            var x = xInput.stringValue();
            var y = yInput.stringValue();
            var bgColor = bgInput.stringValue();

            //Call the createRect function passing the user's input
            createRect(x, y, width, height, bgColor);

        } else {
            return null
        }
    }

    function createRect(x, y, width, height, bgColor) {

        sketch.selectedDocument.selectedPage.iterate(function(artboard){
            
            var customStyle = new sketch.Style()
            customStyle.fills = ["#" + bgColor];

            var createdRect = artboard.newShape({frame: new sketch.Rectangle(x, y, width, height), style: customStyle, name: "rect-" + width + "-" + height});
            
            selection.clear()
            createdRect.addToSelection()

        });

    }

    showAlertWindow();
};