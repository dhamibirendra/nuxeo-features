
// EDITOR

if (typeof NXThemesEditor == "undefined") {
    NXThemesEditor = {
        writeMessage: function(msg) {
          var box = $("nxthemesStatusMessage");
          box.innerHTML = msg;
          box.show();
          NXThemes.Effects.get('fadeout')(box, {delay: 4000});
        },
        isLowerCase: function(s) {
          for (var i = 0; i < s.length; i= i+1) {
            var c = s.charAt(i);
            if ( !((c>="a") && (c<="z")) ) {
              return false;
            }
          }
          return true;
        },
        isLowerCaseOrSpace: function(s) {
          for (var i = 0; i < s.length; i= i+1) {
            var c = s.charAt(i);
            if ( !((c>="a") && (c<="z") || c == ' ')) {
              return false;
            }
          }
          return true;
        },              
	extractElementUid: function(el) {
	  var attr = el.getAttribute('id');
	  if (!attr) {
	    return
	  }
	  return attr.replace(/^e/, '');
	}
    };

};

NXThemesEditor.accessDenied = function() {
    alert('Administrator rights are required to access the theme editor.');
    NXThemes.expireCookie("nxthemes.theme");
    NXThemes.expireCookie("nxthemes.engine");
    NXThemes.expireCookie("nxthemes.mode");
    NXThemes.expireCookie("nxthemes.perspective");
    window.location.reload();  
};

NXThemesEditor.setViewMode =  function(mode) {
    NXThemes.setCookie("nxthemes.mode", mode);
    NXThemes.getViewById("view modes").refresh();
    NXThemesEditor.refreshCanvas();
};

NXThemesEditor.deletePage = function(pagePath) {
    var ok = confirm("Deleting page, are you sure?");
    if (!ok) {
        return;
    }
    var i = pagePath.indexOf('/');
    if (i <= 0) {
    	return;
    }
    var pageName = pagePath.substr(i+1);
    if (pageName == "default") {
    	window.alert("Cannot delete the default page");
    	return;
    }
    var themeName = pagePath.substr(0, i);
    var url = nxthemesBasePath + "/nxthemes-editor/delete_page";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             page_path: pagePath
         },
         onSuccess: function(r) {
        	 NXThemesEditor.selectTheme(themeName + "/default");
             NXThemesEditor.refreshPageSelector();
             NXThemes.getViewById("theme actions").refresh();
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }      
    });
};

NXThemesEditor.moveElement = function(info) {
    var srcId = NXThemesEditor.extractElementUid(info.source);
    var destId = NXThemesEditor.extractElementUid(info.target);
    var order = info.order;
    var url = nxthemesBasePath + "/nxthemes-editor/move_element";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             src_id: srcId,
             dest_id: destId,
             order: order
         },
         onSuccess: function(r) {
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.addFragment = function(typeName, destId) {
    var url = nxthemesBasePath + "/nxthemes-editor/insert_fragment";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             type_name: typeName,
             dest_id: destId
         },
         onSuccess: function(r) {
             NXThemesEditor.switchToCanvas();
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
    return false;
};

NXThemesEditor.editElement = function(info) {
    var id = NXThemesEditor.extractElementUid(info.target);
    var url = nxthemesBasePath + "/nxthemes-editor/select_element";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'id': id
         },
         onSuccess: function(r) {
             NXThemes.getControllerById("editor perspectives").switchTo("element editor");
             NXThemes.getViewById("element editor tabs").switchTo("element editor perspectives/edit properties");             
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.insertFragment = function(info) {
    var id = NXThemesEditor.extractElementUid(info.target);
    var url = nxthemesBasePath + "/nxthemes-editor/select_element";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'id': id
         },
         onSuccess: function(r) {
             NXThemes.getControllerById("editor perspectives").switchTo("fragment factory");
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.manageStyles = function() {
    NXThemes.getControllerById("editor perspectives").switchTo('style manager');
    NXThemesStyleManager.setEditMode('named styles');
};

NXThemesEditor.changeElementStyle = function(info) {
    var id = NXThemesEditor.extractElementUid(info.target);
    var url = nxthemesBasePath + "/nxthemes-editor/select_element";

    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'id': id
         },
         onSuccess: function(r) {
             NXThemes.getControllerById("editor perspectives").switchTo("element editor");
             NXThemes.getViewById("element editor tabs").switchTo("element editor perspectives/edit style");
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.setSize = function(info) {
    var form = Event.findElement(info, "form");
    var id = null;
    var width = null;
    $A(Form.getElements(form)).each(function(i) {
        var name = i.name;
        var value = $F(i);
        if (name == "id") {
          id = value;
        } else if (name == "width") {
          width = value;
        }
      });
    var url = nxthemesBasePath + "/nxthemes-editor/update_element_width"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'id': id,
             'width': width
         },
         onSuccess: function(r) {
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });       
};

NXThemesEditor.updateElementProperties = function(info) {
    var form = Event.findElement(info, "form");
    var id = null;
    var propertyMap = $H();
    $A(Form.getElements(form)).each(function(i) {
        var name = i.name;
        var value = $F(i);
        if (name == "id") {
            id = value;
        } else {
			if (name.match(":lines$") == ":lines") {
				name = name.replace(/:lines$/, '');
				var lines = value.split(/\r|\n|\r\n/);
				value = "";
				var len = lines.length;
				for (i=0; i<len; i++) {
					var line = lines[i];
					value += '"' + line.replace(/"/g, '""') + '"';
				    if (i < len - 1) {
						value += ',';
					}
				}
			}
            if (i.type == 'checkbox') {
                value = value == 'on' ? "true" : "false";
            }
        	propertyMap.set(name, value);
        }
    });
    var url = nxthemesBasePath + "/nxthemes-editor/update_element_properties"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             id: id,
             property_map: propertyMap.toJSON()
         },
         onSuccess: function(r) {
             NXThemes.getViewById("element properties").refresh();
             NXThemesEditor.refreshUndoActions();
             NXThemesEditor.writeMessage("Element properties updated.");
         },
         onFailure: function(r) {
             NXThemes.getViewById("element properties").refresh();
             NXThemesEditor.writeMessage("Properties could not be updated.");
         }         
    });
};

NXThemesEditor.updateElementWidget = function(info) {
    var form = Event.findElement(info, "form");
    var id = null;
    var viewName = "";
    $A(Form.getElements(form)).each(function(i) {
        var name = i.name;
        var value = $F(i);
        if (name == "id") {
          id = value;
        } else if (name == "viewName") {
          viewName = value;
        }
    });
    if (!viewName) {
        return;
    }
    var url = nxthemesBasePath + "/nxthemes-editor/update_element_widget"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'id': id,
             'view_name': viewName
         },
         onSuccess: function(r) {
             NXThemesEditor.refreshUndoActions();
             NXThemesEditor.writeMessage("Widget view changed.");
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};


NXThemesEditor.updateElementDescription = function(info) {
    var form = Event.findElement(info, "form");
    var id = null;
    var description = "";
    $A(Form.getElements(form)).each(function(i) {
        var name = i.name;
        var value = $F(i);
        if (name == "id") {
          id = value;
        } else if (name == "description") {
          description = value;
        }
    });

    if (!description) {
      return;
    }
    var url = nxthemesBasePath + "/nxthemes-editor/update_element_description"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'id': id,
             'description': description
         },
         onSuccess: function(r) {
             NXThemesEditor.refreshUndoActions();
             NXThemesEditor.writeMessage("Description changed.");
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });         
};


NXThemesEditor.updateElementStyle = function() {
    var form = $('nxthemesElementStyle');
    var id, path, viewName;
    var propertyMap = $H();
    $A(Form.getElements(form)).each(function(i) {
        var name = i.name;
        var value = $F(i);
        if (name == "id") {
          id = value;
        } else if (name == "path") {
          path = value;
        } else if (name == "viewName") {
          viewName = value;
        } else if (name.indexOf('property:') === 0) {
          propertyMap.set(name.substr(9), value);
        }
    });
    
    var url = nxthemesBasePath + "/nxthemes-editor/update_element_style"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             id: id,
             view_name: viewName,
             path: path,
             property_map: propertyMap.toJSON()
         },
         onSuccess: function(r) {
             NXThemesStyleEditor.refreshCssPreview();
             NXThemesEditor.refreshUndoActions();
             NXThemesEditor.writeMessage("Style updated.");
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.updateElementStyleCss = function() {
    var form = $('nxthemesElementStyleCSS');
    var cssSource, viewName, id;
    Form.getElements(form).each(function(i) {
        var name = i.name;
        var value = $F(i);
        if (name == "cssSource") {
          cssSource = value;
        } else if (name == "id") {
          id = value;
        } else if (name == "viewName") {
          viewName = value;
        }
    });
    var url = nxthemesBasePath + "/nxthemes-editor/update_element_style_css"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             id: id,
             view_name: viewName,
             css_source: cssSource
         },
         onSuccess: function(r) {
             NXThemesStyleEditor.refreshCssPreview();
             NXThemesEditor.refreshUndoActions();
             NXThemesEditor.writeMessage("Style updated.");
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.setElementVisibility = function(info) {
    var form = Event.findElement(info, "form");
    var id = null;
    var alwaysVisible = false;
    var perspectives = [];
    Form.getElements(form).each(function(i) {
        var name = i.name;
        var value = $F(i);
        if (name == "id") {
          id = value;
        } else if (name == "alwaysVisible") {
          alwaysVisible = value ? true : false;
        } else if (name == "perspectives") {
          perspectives = $F("perspectives");
        }
    });
    var url = nxthemesBasePath + "/nxthemes-editor/update_element_visibility"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'id': id,
             'always_visible': alwaysVisible,
             'perspectives': perspectives
         },
         onSuccess: function(r) {
             NXThemes.getViewById("element visibility").refresh();
             NXThemesEditor.refreshUndoActions();
             NXThemesEditor.writeMessage("Visibility changed.");
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.setElementWidget = function(info) {
    var id = NXThemesEditor.extractElementUid(info.target);
    if (id === null) {
      return;
    }
    var viewName = info.options.choice;
    if (!viewName) {
        return;
    }
    var url = nxthemesBasePath + "/nxthemes-editor/update_element_widget"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'id': id,
             'view_name': viewName
         },
         onSuccess: function(r) {
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.copyElement = function(info) {
    var id = NXThemesEditor.extractElementUid(info.target);
    var url = nxthemesBasePath + "/nxthemes-editor/copy_element"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             id: id
         },
         onSuccess: function(r) {
             NXThemesEditor.writeMessage("Element copied.");
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.splitElement = function(info) {
    var id = NXThemesEditor.extractElementUid(info.target);
    var url = nxthemesBasePath + "/nxthemes-editor/split_element"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             id: id
         },
         onSuccess: function(r) {
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });       
};

NXThemesEditor.setElementPadding = function(info) {
    var id = NXThemesEditor.extractElementUid(info.target);
    var url = nxthemesBasePath + "/nxthemes-editor/select_element"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'id': id
         },
         onSuccess: function(r) {
             NXThemes.getControllerById("editor perspectives").switchTo("padding editor");
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.updateElementPadding = function(info) {
    var form = Event.findElement(info, "form");
    var id = null;
    var propertyMap = $H();
    $A(Form.getElements(form)).each(function(i) {
        var name = i.name;
        var value = $F(i);
        propertyMap.set(i.name, value);
    });    
    var url = nxthemesBasePath + "/nxthemes-editor/update_element_layout"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             property_map: propertyMap.toJSON()
         },
         onSuccess: function(r) {
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.alignElement = function(info) {
    var target = info.target;
    var id = NXThemesEditor.extractElementUid(info.target);
    if (id === null) {
      return;
    }
    var position = info.options.choice;
    var url = nxthemesBasePath + "/nxthemes-editor/align_element"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             id: id,
             position: position
         },
         onSuccess: function(r) {
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });     
};

NXThemesEditor.duplicateElement = function(info) {
    var id = NXThemesEditor.extractElementUid(info.target);
    var url = nxthemesBasePath + "/nxthemes-editor/duplicate_element"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             id: id
         },
         onSuccess: function(r) {
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }
    });       
};

NXThemesEditor.pasteElement = function(info) {
    var destId = NXThemesEditor.extractElementUid(info.target);
    var url = nxthemesBasePath + "/nxthemes-editor/paste_element"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             dest_id: destId
         },
         onSuccess: function(r) {
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
             NXThemesEditor.writeMessage("Element pasted.");
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }  
    });
};

NXThemesEditor.deleteElement = function(info) {
    var id = NXThemesEditor.extractElementUid(info.target);
    var url = nxthemesBasePath + "/nxthemes-editor/delete_element";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             id: id
         },
         onSuccess: function(r) {
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }
    });
};

NXThemesEditor.selectTheme = function(name) {
    if (name) {
        NXThemes.setCookie("nxthemes.theme", name);
    } else {
    	NXThemes.expireCookie("nxthemes.theme");
    }
};

NXThemesEditor.switchTheme = function(info) {
    var form = Event.findElement(info, "form");
    var name = Form.findFirstElement(form).getValue();
    if (name) {
        NXThemesEditor.selectTheme(name);
        NXThemesEditor.refreshThemeSelector();
        NXThemesEditor.refreshPageSelector();
        NXThemes.getViewById("theme actions").refresh();
        NXThemesEditor.refreshCanvas();
    } else {
        NXThemes.getControllerById('editor perspectives').switchTo('theme browser');
    }
};

NXThemesEditor.selectPerspective = function(info) {
    var form = Event.findElement(info, "form");
    var perspective = Form.findFirstElement(form).getValue();
    NXThemes.setCookie("nxthemes.perspective", perspective);
    NXThemesEditor.refreshCanvas();
};

NXThemesEditor.addThemeToWorkspace = function(name, viewId) {
    var url = nxthemesBasePath + "/nxthemes-editor/add_theme_to_workspace"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             name: name
         },          
         onSuccess: function(r) {
             NXThemes.getViewById(viewId).refresh();
        	 NXThemesEditor.refreshThemeSelector();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.removeThemeFromWorkspace = function(name, viewId) {
    var url = nxthemesBasePath + "/nxthemes-editor/remove_theme_from_workspace"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             name: name
         },          
         onSuccess: function(r) {
             NXThemes.getViewById(viewId).refresh();
        	 NXThemesEditor.refreshThemeSelector();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};


NXThemesEditor.switchPage = function(info) {
    var target = Event.element(info);
    var name = target.getAttribute("name");
    if (name !== null) {
        NXThemesEditor.selectTheme(name);
        NXThemesEditor.refreshPageSelector();
        NXThemes.getViewById("theme actions").refresh();
        NXThemesEditor.refreshCanvas();
    }
};

NXThemesEditor.addTheme = function(viewid) {
    var name = prompt("Enter a theme name:", "");
    if (name === "") {
        window.alert("Theme names cannot be empty.");
        return "";
    }
    if (!name.match(/^([a-z]|[a-z][a-z0-9_\-]*?[a-z0-9])$/)) {
        window.alert("Theme names may only contain lower-case alpha-numeric characters, digits, underscores and dashes");
        return "";
    }
    var url = nxthemesBasePath + "/nxthemes-editor/add_theme";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             name: name
         },
         onSuccess: function(r) {
             var text = r.responseText;
             NXThemesEditor.selectTheme(text);
             NXThemes.getViewById(viewid).refresh();
             NXThemesEditor.refreshPageSelector();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.addPage = function(themeName) {
    var name = prompt("Please enter a page name:", "");
    if (name === "") {
        window.alert("Page names cannot be empty.");
        return "";
    }
    if (!name.match(/^([a-z]|[a-z][a-z0-9_\-]*?[a-z0-9])$/)) {
        window.alert("Page names may only contain lower-case alpha-numeric characters, digits, underscores and dashes.");
        return "";
    }
    var url = nxthemesBasePath + "/nxthemes-editor/add_page";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             path: themeName + '/' + name
         },
         onSuccess: function(r) {
             var text = r.responseText;
             NXThemesEditor.selectTheme(text);
             NXThemes.getViewById("page selector").refresh();
             NXThemes.getViewById("theme actions").refresh();
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }            
    });    
};

NXThemesEditor.switchToCanvas = function() {
    NXThemes.getControllerById('editor perspectives').switchTo('canvas editor');
};

NXThemesEditor.backToCanvas = function() {
	NXThemesEditor.switchToCanvas();
    NXThemesEditor.refreshCanvas();
    NXThemes.getControllerById('editor buttons').select();
}

NXThemesEditor.managePresets = function() {
    NXThemes.getControllerById("editor perspectives").switchTo('preset manager');
    NXThemesPresetManager.setEditMode('theme presets');
};

NXThemesEditor.addPreset = function(themeName, category, view_id) {
    var name = prompt("Please enter a preset name:", "");
    if (name === "") {
        window.alert("Preset names cannot be empty.");
        return "";
    }
    if (!NXThemesEditor.isLowerCaseOrSpace(name)) {
        window.alert("Preset names may only contain lower case characters and spaces.");
        return "";
    }
    var url = nxthemesBasePath + "/nxthemes-editor/add_preset";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             theme_name: themeName,
             preset_name: name,
             category: category
         },
         onSuccess: function(r) {
             NXThemes.getViewById(view_id).refresh();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }
    });     
};


NXThemesEditor.editPreset = function(themeName, presetName, value, view_id) {
    var value = prompt("Enter a CSS value:", value);
    if (!value) {
  	  return;
    }
    var url = nxthemesBasePath + "/nxthemes-editor/edit_preset";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             theme_name: themeName,
             preset_name: presetName,
             value: value
         },
         onSuccess: function(r) {
             NXThemes.getViewById(view_id).refresh();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });      
};
 
NXThemesEditor.addSection = function(info) {
    var target = Event.element(info);
    var id = target.getAttribute('sectionid') || target.getAttribute('pageid');
    var url = nxthemesBasePath + "/nxthemes-editor/insert_section_after";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             id: id
         },         
         onSuccess: function(r) {
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.alignSection = function(info, position) {
    var target = Event.element(info);
    var id = target.getAttribute('sectionid');;
    var url = nxthemesBasePath + "/nxthemes-editor/align_element"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             id: id,
             position: position
         }, 
         onSuccess: function(r) {
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    }); 
};

NXThemesEditor.alignSectionLeft = function(info) {
    NXThemesEditor.alignSection(info, "left");
}

NXThemesEditor.alignSectionCenter = function(info) {
    NXThemesEditor.alignSection(info, "center");
}

NXThemesEditor.alignSectionRight = function(info) {
    NXThemesEditor.alignSection(info, "right");
}


NXThemesEditor.setAreaStyle = function(info) {
    var target = Event.element(info);
    var property = target.getAttribute('name');
    NXThemesEditor.currentProperty = property;
    var area = NXThemes.Canvas.getFirstParentNodeWithAnId(target);
    if (area !== null) {
        var id = NXThemesEditor.extractElementUid(area);
        if (id !== null) {
          NXThemesEditor.selectedElement = id;
        }
    }
    var category = null;
    if (property == 'background') {
      category = 'background';
    } else if (property == 'border-top') {
      category = 'border';
    } else if (property == 'border-left') {
      category = 'border';
    } else if (property == 'border-bottom') {
      category = 'border';
    } else if (property == 'border-right') {
      category = 'border';
    }
    var url = nxthemesBasePath + "/nxthemes-editor/select_style_category"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             category: category
         },          
         onSuccess: function(r) {
             NXThemes.getControllerById('area style perspectives').switchTo('style chooser');
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.updateAreaStyle = function(value) {
  NXThemesEditor.closeAreaStyleChooser();
  var element_id = NXThemesEditor.selectedElement;
  if (element_id === null) {
    return;
  }
  var property = NXThemesEditor.currentProperty;
  if (property !== null) {
      if (value == null) {
          value = '';
      }
      var url = nxthemesBasePath + "/nxthemes-editor/assign_style_property"; 
      new Ajax.Request(url, {
         method: 'post',
         parameters: {
             element_id: element_id,
             property: property,
             value: value
         }, 
         onSuccess: function(r) {
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
      });
  }
};

NXThemesEditor.setPresetGroup = function(select) {
    var group = select.value;
    var url = nxthemesBasePath + "/nxthemes-editor/select_preset_group"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             group: group
         }, 
         onSuccess: function(r) {
             NXThemes.getViewById("area style chooser").refresh();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.closeAreaStyleChooser = function() {
    NXThemes.getControllerById('area style perspectives').switchTo('default');
};

NXThemesEditor.exit = function() {
  NXThemes.expireCookie("nxthemes.theme");
  NXThemes.expireCookie("nxthemes.engine");
  NXThemes.expireCookie("nxthemes.mode");
  NXThemes.expireCookie("nxthemes.perspective");
  var url = nxthemesBasePath + "/nxthemes-editor/clear_selections"; 
  new Ajax.Request(url, {
         method: 'post',
         onSuccess: function(r) {
             window.location.reload();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
  });
};

NXThemesEditor.repairTheme = function(src) {
    var url = nxthemesBasePath + "/nxthemes-editor/repair_theme"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
    	     src: src
         },
         onSuccess: function(r) {
             NXThemesEditor.refreshCanvas();
             NXThemesEditor.refreshUndoActions();
             NXThemesEditor.writeMessage("Theme repaired.");
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesEditor.loadTheme = function(src, confirmation) {
    if (confirmation) {
        var ok = confirm("Unsaved changes will be lost, are you sure?");
        if (!ok) {
            return;
        }
    }
    var url = nxthemesBasePath + "/nxthemes-editor/load_theme"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             src: src
         },
         onSuccess: function(r) {
           NXThemesEditor.refreshCanvas();
           NXThemesEditor.refreshThemeSelector();
           NXThemesEditor.refreshPageSelector();
           NXThemesEditor.refreshUndoActions();
           NXThemesEditor.writeMessage("Theme loaded.");
         },
         onFailure: function(r) {
           var text = r.responseText;
           window.alert(text);
         }          
    });
};

NXThemesEditor.deleteTheme = function(src) {
    var ok = confirm("Deleting theme, are you sure?");
    if (!ok) {
        return;
    }
    var url = nxthemesBasePath + "/nxthemes-editor/delete_theme"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             src: src
         },
         onSuccess: function(r) {
           NXThemesEditor.refreshCanvas();
           NXThemesEditor.refreshUndoActions();
           NXThemesEditor.refreshThemeSelector();
           NXThemesEditor.writeMessage("Theme deleted.");
         },
         onFailure: function(r) {
           var text = r.responseText;
           window.alert(text);
         }              
    });
};

NXThemesEditor.refreshCanvas = function() {
    NXThemes.getViewById("canvas area").refresh();
};

NXThemesEditor.refreshUndoActions = function() {
    NXThemes.getViewById("undo actions").refresh();
};

NXThemesEditor.refreshThemeSelector = function() {
    NXThemes.getViewById("theme selector").refresh();
};

NXThemesEditor.refreshPageSelector = function() {
    NXThemes.getViewById("page selector").refresh();
};

NXThemesEditor.undo =  function(theme_name) {
    var url = nxthemesBasePath + "/nxthemes-editor/undo"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             theme_name: theme_name
         },
         onSuccess: function(r) {
           var message = r.responseText;
           var perspective = NXThemes.getControllerById('editor perspectives').getCurrentPerspective();
           switch(perspective) {
               case "preset manager":
                   NXThemes.getViewById("preset manager").refresh();
                   break;
               case "style manager":
                   NXThemes.getViewById("style manager").refresh();
                   break;
               case "element editor":
                   NXThemes.getViewById("element editor").refresh();
                   break;                   
               case "canvas editor":
                   NXThemesEditor.refreshCanvas();
                   break;                                      
           }
           NXThemesEditor.refreshUndoActions();
           NXThemesEditor.refreshPageSelector();
           NXThemesEditor.writeMessage("Undo: " + message);
         },
         onFailure: function(r) {
           var text = r.responseText;
           window.alert(text);
         }              
    });
};

// Initialization

NXThemes.addActions({
    'move element': NXThemesEditor.moveElement,
    'insert fragment': NXThemesEditor.insertFragment,
    'edit element': NXThemesEditor.editElement,
    'duplicate element': NXThemesEditor.duplicateElement,
    'update element properties': NXThemesEditor.updateElementProperties,
    'update element widget': NXThemesEditor.updateElementWidget,
    'update element style': NXThemesEditor.updateElementStyle,
    'update element style css': NXThemesEditor.updateElementStyleCss,
    'update element description': NXThemesEditor.updateElementDescription,
    'set element visibility': NXThemesEditor.setElementVisibility,
    'update element padding': NXThemesEditor.updateElementPadding,
    'copy element': NXThemesEditor.copyElement,
    'paste element': NXThemesEditor.pasteElement,
    'delete element': NXThemesEditor.deleteElement,
    'select perspective': NXThemesEditor.selectPerspective,
    'switch theme': NXThemesEditor.switchTheme,
    'switch page': NXThemesEditor.switchPage,
    'set size': NXThemesEditor.setSize,
    'add section': NXThemesEditor.addSection,
    'align section left': NXThemesEditor.alignSectionLeft,
    'align section center': NXThemesEditor.alignSectionCenter,
    'align section right': NXThemesEditor.alignSectionRight,        
    'align element': NXThemesEditor.alignElement,
    'split element': NXThemesEditor.splitElement,
    'set element padding': NXThemesEditor.setElementPadding,
    'set element widget': NXThemesEditor.setElementWidget,
    'set area style': NXThemesEditor.setAreaStyle,
    'change element style': NXThemesEditor.changeElementStyle,
    'cancel event': function(info) {Event.stop(info);}
});

// Filters
NXThemesEditor.cleanUpCanvas = function(html) {
    // remove inline xmlns="..."
    html = html.replace(/xmlns="(.*?)"/g, "");
    // remove inline onclick="..."
    html = html.replace(/onclick="(.*?)"/g, "");
    return html;
};

NXThemes.registerFilters({
    'clean up canvas': NXThemesEditor.cleanUpCanvas
});

//PRESET MANAGER

if (typeof NXThemesPresetManager == "undefined") {
    NXThemesPresetManager = {
    }
}

NXThemesPresetManager.refresh = function() {
    NXThemes.getViewById("preset manager").refresh();
};

NXThemesPresetManager.setEditMode = function(mode) {
    var url = nxthemesBasePath + "/nxthemes-editor/select_preset_manager_mode"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             mode: mode
         },
         onSuccess: function(r) {
        	 NXThemesPresetManager.refresh();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};


NXThemesPresetManager.selectPresetCategory = function(category) {
    var url = nxthemesBasePath + "/nxthemes-editor/select_preset_category"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             category: category
         },
         onSuccess: function(r) {
        	 NXThemesPresetManager.refresh();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};


NXThemesPresetManager.editPreset = function(info) {
  var target = Event.element(info);
  var model = info.model;
  var data = model.getData();
  var themeName = data.get('theme_name');
  var presetName = data.get('name');
  var value = data.get('value');
  value = prompt("Enter a CSS value:", value);
  if (!value) {
	  return;
  }
  var url = nxthemesBasePath + "/nxthemes-editor/edit_preset";
  new Ajax.Request(url, {
       method: 'post',
       parameters: {
           theme_name: themeName,
           preset_name: presetName,
           value: value
       },
       onSuccess: function(r) {
           NXThemesPresetManager.refresh();
           NXThemesEditor.refreshUndoActions();
       },
       onFailure: function(r) {
           var text = r.responseText;
           window.alert(text);
       }       
  });          
}

NXThemesPresetManager.renamePreset = function(info) {
  var target = Event.element(info);
  var model = info.model;
  var data = model.getData();
  var themeName = data.get('theme_name');
  var oldName = data.get('name');
  var newName = prompt("Enter a preset name:", oldName);
  var url = nxthemesBasePath + "/nxthemes-editor/rename_preset";
  new Ajax.Request(url, {
       method: 'post',
       parameters: {
           theme_name: themeName,
           old_name: oldName,
           new_name: newName
       },
       onSuccess: function(r) {
           NXThemesPresetManager.refresh();
           NXThemesEditor.refreshUndoActions();
       },
       onFailure: function(r) {
           var text = r.responseText;
           window.alert(text);
       }
  });          
}

NXThemesPresetManager.copyPreset = function(info) {
  var target = Event.element(info);
  var model = info.model;
  var data = model.getData();
  var id = data.get('id');
  var url = nxthemesBasePath + "/nxthemes-editor/copy_preset"; 
  new Ajax.Request(url, {
       method: 'post',
       parameters: {
           id: id
       },
       onSuccess: function(r) {
           NXThemesEditor.writeMessage("Preset copied.");
       },
       onFailure: function(r) {
           var text = r.responseText;
           window.alert(text);
       }       
  });    
}

NXThemesPresetManager.pastePreset = function(info) {
  var target = Event.element(info);
  var model = info.model;
  var data = model.getData();
  var themeName = data.get('theme_name');
  var presetName = prompt("Enter a preset name:");
  var url = nxthemesBasePath + "/nxthemes-editor/paste_preset"; 
  new Ajax.Request(url, {
       method: 'post',
       parameters: {
           theme_name: themeName,
           preset_name: presetName
       },
       onSuccess: function(r) {
           NXThemesPresetManager.refresh();
           NXThemesEditor.refreshUndoActions();
       },
       onFailure: function(r) {
           var text = r.responseText;
           window.alert(text);
       }  
  });    
}

NXThemesPresetManager.deletePreset = function(info) {
  var target = Event.element(info);
  var model = info.model;
  var data = model.getData();
  var themeName = data.get('theme_name');
  var presetName = data.get('name');
  var url = nxthemesBasePath + "/nxthemes-editor/delete_preset"; 
  new Ajax.Request(url, {
       method: 'post',
       parameters: {
           theme_name: themeName,
           preset_name: presetName
       },
       onSuccess: function(r) {
           NXThemesPresetManager.refresh();
           NXThemesEditor.refreshUndoActions();
       },
       onFailure: function(r) {
           var text = r.responseText;
           window.alert(text);
       }  
  });    
}

NXThemesPresetManager.setPresetCategory = function(info) {
	var target = Event.element(info);
	var model = info.model;
	var data = model.getData();
    var category = info.options.choice;
    if (!category) {
        return;
    }
    var themeName = data.get('theme_name');
    var presetName = data.get('name');
    var url = nxthemesBasePath + "/nxthemes-editor/set_preset_category";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             theme_name: themeName,
             preset_name: presetName,
             category: category
         },
         onSuccess: function(r) {
             NXThemesPresetManager.refresh();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }
    });         
};

NXThemesPresetManager.addMissingPreset = function(themeName, presetName) {
    var presetValue = prompt("Enter a CSS value:", "");
    if (!presetValue) {
        return;
    } 
    var url = nxthemesBasePath + "/nxthemes-editor/add_preset";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             theme_name: themeName,
             preset_name: presetName,
             category: "",
             value: presetValue
         },
         onSuccess: function(r) {
             NXThemesPresetManager.refresh();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }
    });     
    
};

NXThemesPresetManager.convertValueToPreset = function(themeName, category, presetValue) {
    var presetName = prompt("Enter a preset name:", "");
    if (!presetName) {
        return;
    } 
    var url = nxthemesBasePath + "/nxthemes-editor/convert_to_preset";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             theme_name: themeName,
             preset_name: presetName,
             category: category,
             value: presetValue
         },
         onSuccess: function(r) {
             NXThemesPresetManager.refresh();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }
    });     
    
};

NXThemesPresetManager.selectPresetGroup = function(group) {
  var url = nxthemesBasePath + "/nxthemes-editor/select_preset_group"; 
  new Ajax.Request(url, {
       method: 'post',
       parameters: {
           group: group
       },
       onSuccess: function(r) {
      	 NXThemesPresetManager.refresh();
       },
       onFailure: function(r) {
           var text = r.responseText;
           window.alert(text);
       }         
  });
};




NXThemes.addActions({
    'edit preset': NXThemesPresetManager.editPreset,
    'set preset category': NXThemesPresetManager.setPresetCategory,
    'rename preset': NXThemesPresetManager.renamePreset,    
    'copy preset': NXThemesPresetManager.copyPreset,
    'paste preset': NXThemesPresetManager.pastePreset,
    'delete preset': NXThemesPresetManager.deletePreset
});


// STYLE EDITOR

if (typeof NXThemesStyleEditor == "undefined") {
    NXThemesStyleEditor = {
        'currentProperty': null
    };
}

NXThemesStyleEditor.refreshPreview = function() {
    var previewArea = document.getElementById('stylePreviewArea');
    var element = previewArea.getAttribute('element');
    NXThemesStyleEditor.renderElement(element, previewArea);
};

NXThemesStyleEditor.refreshCssPreview = function() {
    var url = nxthemesBasePath + "/nxthemes-editor/render_css_preview"; 
    new Ajax.Request(url, {
         method: 'get',
         onSuccess: function(r) {
           var text = r.responseText;
           text = text.replace('${basePath}', nxthemesBasePath);
           $('previewCss').innerHTML = text;
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesStyleEditor.closeStylePicker = function() {
    NXThemes.getControllerById('style editor perspectives').switchTo('style properties');
};

NXThemesStyleEditor.renderElement = function(id, area) {
    // render an element inside an area
    var options = {
      method: 'get',
      onSuccess: function(req) {
        var temp = document.createElement("div");
        temp.innerHTML = req.responseText;
        var elementList = $(temp).select('#e' + id);
        if (elementList.length > 0) {
          var element = elementList[0];
          var html = element.innerHTML;
          // remove inline xmlns="..."
          html = html.replace(/xmlns="(.*?)"/g, "");
          // remove inline onclick="..."
          html = html.replace(/onclick="(.*?)"/g, "");
          area.innerHTML = html;
        }
      }
    };
    var url = window.location.href;
    var i = url.indexOf('?');
    var query_params = $H({'engine': 'fragments-only'});
    if (i > 0) {
      var query_string = url.substr(i+1);
      query_params = query_params.update($H(query_string.toQueryParams()));
      url = url.substr(0, i);
    }
    url = url + '?' + query_params.toQueryString();
    new Ajax.Request(url, options);
};

NXThemesStyleEditor.chooseStyleSelector = function(select) {
  var value = select.value;
  if (value === '') {
    value = null;
  }
  NXThemesStyleEditor.setStyleSelector(select.value);
};

NXThemesStyleEditor.setPresetGroup = function(select) {
    var group = select.value;
    var url = nxthemesBasePath + "/nxthemes-editor/select_preset_group"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             group: group
         },
         onSuccess: function(r) {
           NXThemesStyleEditor.refreshStylePicker();
           NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesStyleEditor.setStyleSelector = function(selector) {
    var url = nxthemesBasePath + "/nxthemes-editor/select_style_selector"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             selector: selector
         },
         onSuccess: function(r) {
        	 NXThemesStyleEditor.setStyleEditMode("form");
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesStyleEditor.createStyle = function() {
    var url = nxthemesBasePath + "/nxthemes-editor/create_style"; 
    new Ajax.Request(url, {
         method: 'post',
         onSuccess: function(r) {
             NXThemes.getViewById("element style").refresh();
             NXThemesEditor.refreshUndoActions();
             NXThemesEditor.writeMessage("New style created.");
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesStyleEditor.getSelectorInContext = function(element, context) {
  if (element == context) {
    return "";
  }
  var i, node, tag, name, selector, classnames;
  node = element;

  // If no context is passed, use the document itself.
  if (!context) {
    context = document;
  }

  // start from the first contained element
  context = context.childNodes.item(0);

  var selectors = new Array();
  while (node) {
    if (node == context) {
      break;
    }
    tag = node.tagName;
    if (!tag) {
      break;
    }
    tag = tag.toLowerCase();
    classnames = node.className;
    name = '';
    if (classnames) {
      name = classnames.replace(' ', '.');
    }
    selector = name ? tag + '.' + name : tag;
    selectors.push(selector);
    node = node.parentNode;
  }

  // reverse the array
  selectors.reverse();
  return selectors.join(" ");
};

NXThemesStyleEditor.refreshEditor = function() {
  NXThemes.getViewById("element style").refresh();
};

NXThemesStyleEditor.selectTag = function(info) {
    var target = info.target;
    Event.stop(info);
    var area = $('stylePreviewArea');
    var selector = NXThemesStyleEditor.getSelectorInContext(target, area);
    NXThemesStyleEditor.setStyleSelector(selector);
    return false;
};

NXThemesStyleEditor.setCurrentStyleLayer = function(uid) {
    var url = nxthemesBasePath + "/nxthemes-editor/select_style_layer"; 
      new Ajax.Request(url, {
         method: 'post',
         parameters: {
             uid: uid
         },
         onSuccess: function(r) {
             NXThemes.getControllerById('style editor perspectives').switchTo('default');
             NXThemesEditor.refreshUndoActions();
             NXThemes.getViewById("element style").refresh();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
      });
};

NXThemesStyleEditor.pickPropertyValue = function(info) {
    var target = info.target;
    var category = target.getAttribute('category');
    NXThemesStyleEditor.currentProperty = target.getAttribute('property');
    var url = nxthemesBasePath + "/nxthemes-editor/select_style_category"; 
      new Ajax.Request(url, {
         method: 'post',
         parameters: {
             category: category
         },
         onSuccess: function(r) {
           NXThemes.getControllerById('style editor perspectives').switchTo('style picker');
           NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
      });
};

NXThemesStyleEditor.setStyleEditMode = function(mode, fromMode) {
    if (fromMode == 'form') {
      NXThemesEditor.updateElementStyle();
    }
    if (fromMode == 'css') {
      NXThemesEditor.updateElementStyleCss();
    }
    var url = nxthemesBasePath + "/nxthemes-editor/select_style_edit_mode"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             mode: mode
         },
         onSuccess: function(req) {
           NXThemes.getControllerById("style editor perspectives").switchTo("style properties");
           NXThemes.getViewById("style properties").refresh();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesStyleEditor.setStylePropertyCategory = function(category) {
    var url = nxthemesBasePath + "/nxthemes-editor/select_style_property_category"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             category: category
         },
         onSuccess: function(req) {
           NXThemes.getViewById("style properties").refresh();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesStyleEditor.makeElementUseNamedStyle = function(select) {
    var value = select.value;
    if (value === '') {
        value = null;
    }
    var form = $(select).up("form");
    var id = form.getAttribute("element");
    var themeName = form.getAttribute("currentThemeName");
    var styleName = value;
    var url = nxthemesBasePath + "/nxthemes-editor/make_element_use_named_style";
    var parameters = {
        id: id,
        theme_name: themeName
    }    
    if (styleName) {
        parameters['style_name'] = styleName;
    }
    new Ajax.Request(url, {
         method: 'post',
         parameters: parameters,
         onSuccess: function(req) {
             NXThemes.getViewById("element style").refresh();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesStyleEditor.createNamedStyle = function(id, currentThemeName, screenName) {
    var styleName = prompt("Please enter a style name:", "");
    if (styleName === null) {
        return;
    }
    if (styleName === "") {
        window.alert("Style names cannot be empty.");
        return;
    }
    var url = nxthemesBasePath + "/nxthemes-editor/create_named_style"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             id: id,
             style_name: styleName,
             theme_name: currentThemeName
         },
         onSuccess: function(req) {
             NXThemes.getViewById(screenName).refresh();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesStyleEditor.deleteNamedStyle = function(id, currentThemeName, styleName) {
    var ok = confirm("Deleting style, are you sure?");
    if (!ok) {
        return;
    }
    var url = nxthemesBasePath + "/nxthemes-editor/delete_named_style"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             id: id,
             style_name: styleName,
             theme_name: currentThemeName
         },
         onSuccess: function(r) {
             NXThemes.getViewById("element style").refresh();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }
    });
};

NXThemesStyleEditor.updateFormField = function(value) {
  var currentProperty = NXThemesStyleEditor.currentProperty;
  if (currentProperty !== null) {
    var propertyName = 'property:' + currentProperty;
    var inputs = Form.getInputs('nxthemesElementStyle', null, propertyName);
    if (inputs !== null) {
      inputs[0].value = value;
    }
  }
  NXThemesStyleEditor.closeStylePicker();
};

NXThemesStyleEditor.refreshStylePicker = function() {
    NXThemes.getViewById("style picker").refresh();
};


// widgets
NXThemes.registerWidgets({

  stylepreview: function(def) {
    var widget = NXThemes.Canvas.createNode({
      tag: 'div',
      classes: ['nxthemesStylePreview']
    });
    return new NXThemesStyleEditor.StylePreview(widget, def);
  }

});

NXThemesStyleEditor.StylePreview = Class.create();
NXThemesStyleEditor.StylePreview.prototype = Object.extend(new NXThemes.View(), {

  setup: function() {
    NXThemesStyleEditor.refreshPreview();
    NXThemesStyleEditor.refreshCssPreview();
  }

});

// actions
NXThemes.addActions({
  'select style tag': NXThemesStyleEditor.selectTag,
  'update style label': function(info) {
    var box = $('labelInfo');
    if (box === null) {
      return;
    }
    var x = Event.pointerX(info);
    var y = Event.pointerY(info);
    var target = info.target;
    var label= NXThemesStyleEditor.getSelectorInContext(target, $('stylePreviewArea'));
    if (!label) {
      label = '.';
    }
    box.innerHTML = label;
    box.setStyle({left: x+'px', top: y-45+'px'});
    box.show();
  },
  'hide style label': function(info) {
    $('labelInfo').hide();
  },
  'pick property value': NXThemesStyleEditor.pickPropertyValue
});


// STYLE MANAGER

if (typeof NXThemesStyleManager == "undefined") {
	NXThemesStyleManager = {
    };
}


NXThemesStyleManager.deleteUnusedStyleView = function(info) {
    var form = Event.findElement(info, "form");
    var themeName, styleUid, viewName;
    $A(Form.getElements(form)).each(function(i) {
        var name = i.name;
        var value = $F(i);
        if (name == "style_uid") {
        	styleUid = value;
        } else if (name == "view_name") {
        	viewName = value;
        } else if (name == "theme_name") {
        	themeName = value;
        }
    });
    var url = nxthemesBasePath + "/nxthemes-editor/delete_style_view";
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             theme_name: themeName,
             view_name: viewName,
             style_uid: styleUid
         },
         onSuccess: function(r) {
             NXThemes.getViewById("style manager").refresh();
             NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }
    });     
};

NXThemesStyleManager.setEditMode = function(mode) {
    var url = nxthemesBasePath + "/nxthemes-editor/select_style_manager_mode"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             mode: mode
         },
         onSuccess: function(req) {
        	 NXThemes.getViewById("style manager").refresh();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesStyleManager.updateNamedStyleCSS = function(form) {
	var style_uid = '';
	var css_source = '';
    $A(Form.getElements(form)).each(function(i) {
        var name = i.name;
        var value = $F(i);
        if (name == "style_uid") {
          style_uid = value;
        } else if (name == "css_source") {
          css_source = value;
        } else if (name == "theme_name") {
          theme_name = value;
        }
    });
    var url = nxthemesBasePath + "/nxthemes-editor/update_named_style_css"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             'style_uid': style_uid,
             'css_source': css_source,
             'theme_name': theme_name
         },
         onSuccess: function(r) {
        	 NXThemes.getViewById("style manager").refresh();
        	 NXThemesEditor.refreshUndoActions();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });   
}

NXThemesStyleManager.setPageStyles = function(themeName, form) {
    var propertyMap = $H();
    $A(Form.getElements(form)).each(function(i) {
        var name = i.name;
        var value = $F(i);
        if (name.startsWith('style_')) {
            propertyMap.set(name.substr(6), value);
        }
    });
    var url = nxthemesBasePath + "/nxthemes-editor/set_page_styles"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             theme_name: themeName,
             property_map: propertyMap.toJSON()
         },
         onSuccess: function(r) {
             NXThemes.getViewById("style manager").refresh();
             NXThemesEditor.refreshUndoActions();
             NXThemesEditor.writeMessage("Page styles changed.");
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });    
}

NXThemesStyleManager.selectNamedStyle = function(uid) {
    var url = nxthemesBasePath + "/nxthemes-editor/select_named_style"; 
      new Ajax.Request(url, {
         method: 'post',
         parameters: {
             uid: uid
         },
         onSuccess: function(r) {
             NXThemes.getViewById("style manager").refresh();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
      });
};

//actions
NXThemes.addActions({
  'delete unused style view': NXThemesStyleManager.deleteUnusedStyleView
});

// Fragment factory
if (typeof NXThemesFragmentFactory == "undefined") {
    NXThemesFragmentFactory = {
    }
}

NXThemesFragmentFactory.selectFragmentType = function(type) {
    var url = nxthemesBasePath + "/nxthemes-editor/select_fragment_type"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
             type: type
         },
         onSuccess: function(r) {
        	 NXThemes.getViewById("fragment factory").refresh();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

NXThemesFragmentFactory.selectView = function(view) {
    var url = nxthemesBasePath + "/nxthemes-editor/select_fragment_view"; 
    new Ajax.Request(url, {
         method: 'post',
         parameters: {
    	     view: view
         },
         onSuccess: function(r) {
        	 NXThemes.getViewById("fragment factory").refresh();
         },
         onFailure: function(r) {
             var text = r.responseText;
             window.alert(text);
         }         
    });
};

