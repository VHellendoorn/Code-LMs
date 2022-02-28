//Tracks mouse cursor
function MousePositionHandler(e)
{

		MouseX = e.clientX;
		MouseY = e.clientY;
		var TempControlName = "";
		TempControlName = e.originalTarget.id;
		if(!TempControlName || TempControlName == ""){TempControlName = e.currentTarget.id;}
		contextMenuControl = TempControlName;

}

//////////Functions to control effects on buttons

function buttonMouseOver(e)
{
	var computerImgName = (e.originalTarget.src).replace('normal.png','hover.png');
	var ComputeMouseOver = e.originalTarget.setAttribute('src',computerImgName);

}

function buttonMouseOut(e)
{
	var buttonSource = e.originalTarget.src;
	if (buttonSource.indexOf('down.png')>-1 ) {
		var computerImgName = (e.originalTarget.src).replace('down.png','normal.png');
		var ComputeMouseOver = e.originalTarget.setAttribute('src',computerImgName);
		}
	else {
		var computerImgName = (e.originalTarget.src).replace('hover.png','normal.png');
		var ComputeMouseOver = e.originalTarget.setAttribute('src',computerImgName);
		}

}

function buttonMouseDown(e)
{

	var computerImgName = (e.originalTarget.src).replace('hover.png','down.png');
	var ComputeMouseOver = e.originalTarget.setAttribute('src',computerImgName);
	
}

function buttonMouseUp(e)
{

	var computerImgName = (e.originalTarget.src).replace('down.png','hover.png');
	var ComputeMouseOver = e.originalTarget.setAttribute('src',computerImgName);

}

//Shows the labels on tabs
function showLabel(event)
{
	var activeTab = PolyActiveTab();
	var activeTabNumber = ((activeTab.id).split('-'))[1];
	var tabNumber = ((event.originalTarget.id).split('-'))[1];
	var tabLabel = document.getElementById('tabPanelLabel-'+tabNumber);
		
	if(tabCollapseState == 1 && tabNumber != activeTabNumber){
			tabLabel.setAttribute('class','tablabelhover');
		}
	if(tabCollapseState == 0 && tabNumber != activeTabNumber){
			tabLabel.setAttribute('class','hidetablabel');
		}

}

//Hides the labels on tabs
function hideLabel(event)
{
	var tabNumber = ((event.originalTarget.id).split('-'))[1];
	var tabLabel = document.getElementById('tabPanelLabel-'+tabNumber);
	tabLabel.setAttribute('class','hidetablabel');	
}



///////////////////   Area below is for mouse drag navigation functionality

//Variables for dragging
var _startX = 0;
var _startY = 0;
var _offsetX = 0;
var _offsetY = 0;
var _dragElement = document.getElementById('browserContainer');
var _dragVertical;
var middleDrag = false;
var zoomDrag = false;
var zoomDragThreshhold = 50;
var macContextStop = true;

function simulateKeyEvent(keycode) {
  var evt = document.createEvent("KeyboardEvent");
  evt.initKeyEvent("keydown", true, true, null, false, false, false, false, keycode, 0);
  document.dispatchEvent(evt);
}

function openContextMenu(event){
		var gContextMenu = document.getElementById("contentAreaContextMenu");
		var thisWindow = document.window; 
		var thisX = event.screenX;
		var thisY = event.clientY;
		if(thisX < 0){thisX = window.screen.width + thisX;}
		
		var target = PolyActiveBrowser();
		gContextMenu.openPopup(thisWindow, "after_end", thisX, thisY, true, true, event); //event.clientX, (event.clientY - 30)  event.screenX, (event.screenY - 30)
}

function OnMouseDown(event){
	var btnCode = event.button;
    if (btnCode == 2) {
    
    	if(thisOS == 'Darwin' || thisOS == 'Linux' ){
    		
    		var thisTargetId = event.target.id;
    		var thisIndex = thisTargetId.indexOf('polyAddressBar-');
			if(thisIndex != 0){
				deActivateContext();
				}
			}
	
    	middleDrag = false;
    	zoomDrag = false;
		window.onmousemove = OnMouseMove;
		window.onmouseup = OnMouseUp;
		_startX = event.screenX;
		_startY = event.screenY;
		_offsetX = document.getElementById('browserContainer').scrollLeft;
		_dragElement = document.getElementById('browserContainer');
	}
}

function OnMouseMove(event){
		var _dragElement = document.getElementById('browserContainer');
		
		//Do horizontal scrolling
		if(zoomDrag == false){ 
				_dragElement.scrollLeft = (_offsetX + _startX - event.screenX); 
			}
		
		//If dragging horizontally, turn off zoom
		var determineX = _startX - event.screenX;
		var absoluteX = 0;
			if(determineX >= 0){absoluteX = determineX;}
			else{absoluteX = (0 - determineX);}
		if (absoluteX > zoomDragThreshhold){ 
				zoomDrag = false; 
				middleDrag = true;
				//Simulate tab key to get out of context menu
				window.setTimeout(simulateKeyEvent, 10, 9);
				}
		
		//Determine distance dragged vertically
		_offsetY = _startY - event.screenY;
		var absoluteY = 0;
			if(_offsetY >= 0){absoluteY = _offsetY;}
			else{absoluteY = (0 - _offsetY);}
		
		//Activate vertical zoom if past threshold	
		if (absoluteY > zoomDragThreshhold && middleDrag == false){
				//Simulate tab key to get out of context menu
				window.setTimeout(simulateKeyEvent, 10, 9);
				
				zoomDrag = true;
				middleDrag = false;
				if (_offsetY > 0){changeZoom(1);}
				else{changeZoom(-1);}
				_startY = event.screenY;
				_offsetY = 0;
				absoluteY = 0;
				
		}
}

function OnMouseUp(event){
    
   if (event.button == 2 && (thisOS == "Darwin" || thisOS == 'Linux') && middleDrag == false && zoomDrag == false){
   		openContextMenu(event);
   }
   
    
    window.onmousemove = null;
	if  (middleDrag == true) { window.setTimeout(simulateKeyEvent, 5, 9);}
    middleDrag = false;
    zoomDrag = false;
    _startX = 0;
    _startY = 0;
    _offsetX = 0;
    _offsetY = 0;

	//Simulate tab key to get out of context menu
    window.setTimeout(activateContext, 10)
    
    
}
	 	
function activateContext(){
	document.getElementById('browserContainer').setAttribute("context", "contentAreaContextMenu");
}

function deActivateContext(){
	document.getElementById('browserContainer').setAttribute("context", "");
}