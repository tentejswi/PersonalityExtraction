var w = window.innerWidth - 1,
   h = window.innerHeight - 1;
// var w = 1279, h = 746;
var colorbrewer = ["rgb(158,1,66)","rgb(213,62,79)","rgb(244,109,67)","rgb(253,174,97)","rgb(254,224,139)","rgb(230,245,152)","rgb(171,221,164)","rgb(102,194,165)","rgb(50,136,189)","rgb(94,79,162)"];
var color = d3.scale.ordinal().range(colorbrewer)
var re = "";
var goldenratio = 1.618;
var clickedCell;
var stringifyJson;

function bckColor(d,i) {
	var dep = d.depth;
	if (dep == 0) return null;
	var d1Parent = d;
	while(--dep > 0){
		d1Parent = d1Parent.parent;
	}
	var retColor = d3.hsl(color(d1Parent.key));
	var m1 = /([a-z]+)\((.*)\)/i.exec(retColor);
	var m2 = m1[2].split(',');
	var h = parseFloat(m2[0]), // degrees
		s = parseFloat(m2[1])/100,
		l = parseFloat(m2[2])/100
		;

	retColor = d3.rgb(d3.hsl(h,s,l*Math.pow(0.6,d.depth-1)));
	return retColor;
}

function fillOpacity(d,i) {
	if (d.key.match(re)) { return 1;}
	else {return 0.2;}
}

function borderColor(d,i) {
	var dep = d.depth;
	var d1Parent = d;
	while(--dep > 0){
		d1Parent = d1Parent.parent;
	}
	var retColor = d3.hsl(color(d1Parent.key));
	var m1 = /([a-z]+)\((.*)\)/i.exec(retColor);
	var m2 = m1[2].split(',');
	var h = parseFloat(m2[0]), // degrees
		s = parseFloat(m2[1]),
		l = parseFloat(m2[2])
		;
	retColor = d3.hsl(h,s/100,l*0.5/100);
	retColor = d3.rgb(retColor);
	
	if (d.depth == 0) {
		return "#fff";
	}
	else {
		return retColor;		
	}
}

function activeTrue(d, i){
    if (d.parent) {
        d3.select(this).select(".rect")
			.style("stroke", "white")
			.style("stroke-width", 1.5)
//			.call(activeTrue(d.parent))
			;
    }
	
	d3.select(this).select(".button")
		.transition()
		.style("opacity",1)
		;
		
	// Make tooltip visibile
	if(d3.select(this).select(".category-text").classed("wordwrapped"))
	tooltip.style("visibility", "visible");
	tooltip.text(d.key);
}

function activeFalse(d, i){
	if (d.parent) {
		d3.select(this).select(".rect")
			.style("stroke", borderColor)
			.style("stroke-width", 2)
			;	
	}
	
	d3.select(this).select(".button")
		.transition()
		.style("opacity",1e-6)
		;
		
	// Hide tooltip
	tooltip.style("visibility", "hidden");
}

function fontSize(d,i) {
	
	var size = d.dx/d.dy < 1/goldenratio ? d.dy/6.8:d.dx/6.8;
	var words = d.key.split(' ');
	var word = words[0];
	var width = d.dx/d.dy<1/goldenratio ? d.dy : d.dx;
	var height = d.dx/d.dy<1/goldenratio ? d.dx : d.dy;
	var length = 0;
		
    d3.select(this).style("font-size", size + "px").text(word);

	while(((this.getBBox().width >= width) || (this.getBBox().height >= height)) && (size > 12))
	{
		size--;
		d3.select(this).style("font-size", size + "px");
    	this.firstChild.data = word;
	}
}

function wordWrap(d, i){
    var words = d.key.split(' ');
    var line = new Array();
    var length = 0;
    var text = "";
	var width = d.dx/d.dy<1/goldenratio ? d.dy : d.dx;
	var height = d.dx/d.dy<1/goldenratio ? d.dx : d.dy;
	var word;
		
    do {
        word = words.shift();
		line.push(word);
		if (words.length)
			this.firstChild.data = line.join(' ') + " " + words[0]; // Check if adding the next word will cause overflow
		else
			this.firstChild.data = line.join(' '); // Last word
			
        length = this.getBBox().width;

        if (length < width && words.length) { // does not cause overflow, continue
            ;
        }
        else { // causes overflow
			text = line.join(' ');
			this.firstChild.data = text;
			
			if (this.getBBox().width > width) { // using only the word we have in line (without the next word) will cause overflow too
				// should add trailing dots to the last word
				text = d3.select(this).select(function() {return this.lastChild;}).text();
				text = text + "...";
				d3.select(this).select(function() {return this.lastChild;}).text(text);
				// break out of this loop since we can't proceed further
				break;
			}
			else
				;

			// If there is some text, tspan it
			if (text != '') {
				d3.select(this).append("svg:tspan")
					.attr("x", 0)
					.attr("dx", "0.15em")
					.attr("dy", "0.9em")
					.text(text);
			}
			else
				;
				
			// If adding this tspan overflowed in height, remove this element and trail the last one with ...
			if(this.getBBox().height > height && words.length) {
				text = d3.select(this).select(function() {return this.lastChild;}).text();
				text = text + "...";
				d3.select(this).select(function() {return this.lastChild;}).text(text);
				d3.select(this).classed("wordwrapped", true);
				// break out of this loop since we can't proceed further
				break;
			}
			else
				;
			// Start afresh for the next tspan line	
			line = new Array();
        }
    } while (words.length);
	
	this.firstChild.data = '';
}
	
var treemap = d3.layout.treemap()
    .size([w, h])
    .children(function(d) { return isNaN(d.value) ? d3.entries(d.value) : null; })
    .value(function(d) { return d.value; })
    .sticky(true)
	;

var svg = d3.select("body").append("svg:svg")
    .style("width", w)
    .style("height", h)
  .append("svg:g")
    .attr("transform", "translate(-.5,-.5)")
	;

var tooltip = d3.select("body")
	.append("div")
	.attr("id", "tooltip")
	.style("position", "absolute")
	.style("z-index", "10")
	.style("visibility", "hidden")
	.text("")
	;

// var user_url = media_path+"user.json";
user_url = "http://ec2-50-19-209-97.compute-1.amazonaws.com/api/interest.php?u="+username;

d3.json(user_url, function(json) {
  stringifyJson = JSON.stringify(json);
  var cell = svg.data(d3.entries(json)).selectAll("g")
		.data(treemap.nodes)
    .enter().append("svg:g")
      .attr("class", "cell")
      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
	  .attr("visibility", function(d) {return d.depth == 0 ? "visible" : "hidden";})
	  .on("click",onClick)
	  .on('mouseover',activeTrue)
	  .on("mousemove", function(){return tooltip.style("top", (event.pageY-10)+"px").style("left",(event.pageX+10)+"px");})
	  .on('mouseout', activeFalse)
	  ;
	
  cell.append("svg:rect")
  	  .attr("class", "rect")
      .attr("width", function(d) { return d.dx-1; })
      .attr("height", function(d) { return d.dy-1; })
      .style("fill", bckColor)
	  .style("stroke", borderColor)
	  .style("stroke-width",2)
	  ;
	  
  cell.filter(function(d){ return d.depth == 0 ? 1 : 0;})
	.append("svg:image")
  	.attr("x",0)
	.attr("y",0)
	.attr("class", "profilePic")
	.attr("width",function(d) {return d.dx;})
	.attr("height",function(d) {return d.dy;})
	.attr("xlink:href",profilePic)
	.attr("preserveAspectRatio","xMidYMid slice")
	.style("opacity",1)
	;
  	
  cell.append("svg:text")
  	.attr("class", "category-text")
  	.attr("id", "category")
  	.attr("x",0)
	.attr("dx", "0.15em")
	.attr("dy", "0.9em")
  	.attr("transform", function(d) { return d.dx/d.dy<1/goldenratio ? "rotate(90,"+d.dx+",0) translate("+d.dx+",0)" : null; })
	  .style("font-family", "arial, sans-serif")
	  .style("font-style", function(d) {return d.children ? "normal" : "italic";})
	  .style("font-weight", function(d) {return d.children ? "bolder" : "normal";})
	  .style("fill","white")
	  .each(fontSize)
	  .each(wordWrap)
	  ;

  if (isAuthenticated) {
  	cell.append("svg:a")
  		.attr("xlink:href", "#")
		.append("svg:image")
  		.attr("id", "edit")
		.attr("class", "button")
		.attr("x", function(d) {return d.dx - 25;})
		.attr("y", function(d) {return 5;})
  		.attr("width", "20px")
    		.attr("height", "20px")
		.attr("xlink:href", "http://upload.wikimedia.org/wikipedia/commons/6/6c/UniversalEditButton2.svg")
		.style("opacity",0)
		.on("click", editCategory)
		;
  }
});
	  
function onClick(d,i) {
	clickedCell = this;
	var datum = d, dep = d.depth;
	
	if (dep == 0){
		svg.selectAll("g.cell").selectAll(".profilePic").remove();
	}
		
    svg.selectAll("g.cell")
	.filter(function(d){
		if(d.parent)
		{
			return d.depth == dep+1 && d.parent == datum;
		}
		else 
		{
			return d.depth == dep+1;
		}
	})
	.attr("visibility","visible")
	;
}

function editCategory(d,i) {
	d3.event.stopPropagation();
	clickedCell = this.parentNode ? this.parentNode.parentNode : this.parentElement.parentElement;

	var editPopup = d3.select("body")
					.append("div")
					.attr("class", "editpopup")
					.style("position", "absolute")
					.style("left",w/4-100+"px")
					.style("top",h/4+"px")
					.style("width","800px")
					.style("height","150px")
					.style("padding","20px")
					.style("background","white")
					.style("border","4px solid black")
					;
							
	editPopup.append("form")
	.attr("action","\"\"")
	.attr("method", "GET")
	.append("input")
		.attr("class", "inputtext")
		.attr("name", "inputbox")
		.attr("id", "category-edit")
		.attr("type","text")
		.attr("value", d.key)
		.attr("size", "40pt")
		.style("color", "black")
		;
		
	editPopup.select("form")
		.append("span")
			.style("padding-left", "25px")
		.append("input")
			.attr("type", "button")
			.attr("name", "button")
			.attr("value", "Rename category")
			.attr("onclick", "editPopupRename(this.form)")
			;
		
	editPopup.append("div")
		.style("padding-bottom", "5px")
		.style("padding-top", "30px")
	;

	editPopup.append("span")
		.style("padding-left", "50px")
		;
				
	editPopup.append("button")
		.attr("class", "button delete")
		.on("click", editPopupDelete)
		.html("Delete category: "+d.key)
		;

	editPopup.append("span")
		.style("padding-left", "300px")
		;
	
	editPopup.append("button")
		.attr("class", "button cancel")
		.on("click", editPopupCancel)
		.html("Cancel")
		;
}

function editPopupRename(form) {
    var renamedTo = form.inputbox.value;
	var d = clickedCell.__data__;
	d.key = renamedTo;
	var oldKey = "\"" + d.key + "\"";
	var newKey = "\"" + renamedTo + "\"";

	d3.select(clickedCell).select("text")
		.each(fontSize)
		.each(wordWrap)
		;
	
	stringifyJson = stringifyJson.replace(oldKey, newKey);
	new Ajax.request('http://ec2-50-19-209-97.compute-1.amazonaws.com/api/edit.php',
	{
		method : 'post',
		parameters : {u: username, json: stringifyJson},
		onFailure : function() {alert('Could not update JSON due to POST error.')}

	});	
	d3.select("body").select("div.editpopup").remove();
}

function editPopupCancel(d,i) {
	d3.select("body").select("div.editpopup").remove();
}

function editPopupDelete(d,i) {
	var datum = clickedCell.__data__;
	deleteWithChildren(datum);
	
	d3.select("body").select("div.editpopup").remove();	
	
	svg.selectAll("g")
		.data(treemap.value(function(d) {return d.value;}))
//		.transition()
		.call(refreshTree)
		;
//	svg.selectAll("g").exit().remove();
}

function refreshTree(d,i) {
	 
	this
	  .transition()
      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
	  ;
	
	this.selectAll(".rect").transition()
      .attr("width", function(d) { return d.dx-1; })
      .attr("height", function(d) { return d.dy-1; })
	  ;
    
  	
	this.selectAll(".category-text")
  		.attr("x",0)
  		.attr("transform", function(d) { return d.dx/d.dy<1/goldenratio ? "rotate(90,"+d.dx+",0) translate("+d.dx+",0)" : null; })
		.attr("dx", function(d) { return d.dx/d.dy<1/goldenratio ? "0.15em" : "0.15em"; })
		.each(fontSize)
		.each(wordWrap)
		;

	this.selectAll("a").selectAll("image").transition()
		.attr("x", function(d) {return d.dx - 25;})
		.attr("y", 5)
		;
}

function deleteWithChildren(datum) {
	datum.value = 0;
	if(datum.children) {
		for (var i=0; i < datum.children.length; i++) {
			deleteWithChildren(datum.children[i]);
		}
	}
	
	var deleteSelection = svg.selectAll("g.cell")
							.filter(function(d){return d==datum;})
							;
	
	// Keep only the g element so that the data joins properly without breaking. Rest is extra
	deleteSelection.selectAll(".rect").remove();					
	deleteSelection.selectAll(".category-text").remove();
	deleteSelection.selectAll("a").remove();
	
	// The other way is to remove the g (and children) element completely, and then use exit.
	// This will however, require a unique key for the categories
//		deleteSelection // to be used with exit selection
//		.remove()
//		;
			
	return;
}

function update(query) {
	if(query != re) {
			re = new RegExp(query,"i");
			svg.selectAll("g.cell")
			.style("fill-opacity", fillOpacity)
			;
	}
}
