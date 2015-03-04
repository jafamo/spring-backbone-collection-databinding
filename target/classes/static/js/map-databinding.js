/**
 * A dynamic table to perform CRUD operations 
 * with Spring databinding capabilities.
 * In this case, the databinding is performed
 * upon a Map at _server-side_.
 */
$(function(){
	// A prototype to describe the model properties to bind at _server-side_.
	var User = Backbone.Model.extend({
		defaults: function() {
			return {
				name: '',
				email: '',
				// the id of this item
				id: null,
				// key is the position in the Map, which may be the id of this item.
				key: null
			};
		}
	});
	
	// A prototype to hold the collection of model instances. 
	var UserList = Backbone.Collection.extend({
		model: User,
		// Prevents Backbone error: "A url property or function must be specified"
		// since no REST API is present to persist the changes 
		localStorage: new Backbone.LocalStorage('spring-map-databinding')
		
	});
	
	// An instance of the collection.
	var users = new UserList;
	
	// A prototype for the view that renders the model, one for each row of the table
	var UserRowView = Backbone.View.extend({
		// Use `tagName` instead of `el` for DOM elements that do not exist until render is called.
		tagName: 'tr',
		// An HTML template to render this view. The code for this template is embedded inside a <sript> tag in the web page.
		template: Handlebars.compile($('#user-row-template').html()),
		// Events bound to the buttons of the template mapped to methods defined in this View.
		events: {
			'click .edit-user-btn': 'editUser',
			'click .done-edit-btn': 'doneEdit',
			'click .delete-user-btn': 'deleteUser'
		},
		// This function will fill-in the template with the model's values
		render: function() {
			var html = this.template(this.model.attributes);
			// WARN: Don't use `this.$el.html(html)`, for it will unbind all events defined above!!!
			this.$el.empty();
			this.$el.append(html);
			return this;
		},
		// This will be called when creating a new instance of this View.
		initialize: function() {
			// If the model underlying this view gets changed, `this.render` will be called
			this.listenTo(this.model, 'change', this.render);
			// The function `this.remove` is already implemented by Backbone.
			this.listenTo(this.model, 'destroy', this.remove);
		},
		// This function gets called when the user clicks on the "Delete" button.
		deleteUser : function(e) {
			var id = this.model.get('id');
			if (id < 0) {
				// It's a newly added User, so let's just destroy the model
				this.model.destroy();
			} else {
				// It's a User retrieved from _server-side_, 
				// so let's keep its `key` on the Map, but set `id=null`
				this.model.set('id', null);
			}
		},
		// This function gets called when the user clicks on the "Delete" button.
		editUser : function(e) {
			// Place an input inside each `td.editable` for the user to enter new data.
			this.$el.find('td.editable').each(function(index, cell) {
				var textbox = $('<input>')
					.attr('type', 'text')
					.attr('class', 'editable')
					.attr('value', $(cell).text());
				$(cell).empty();
				$(cell).append(textbox);
			});
			// Toggle the button from "Edit" to "Done".
			this.$el.find('.edit-user-btn')
				.val('Done')
				.removeClass('edit-user-btn')
				.addClass('done-edit-btn');
		},
		// This function gets called when the user clicks on the "Done" button.
		doneEdit : function(e) {
			var attributes = {};
			// Traverse the `td.editable` to gather enterd values.
			this.$el.find('td.editable').each(function(index, cell) {
				var attrName = $(cell).attr('data-model-attr');
				var attrValue = $(cell).find('input').val();
				attributes[attrName] = attrValue;
			});
			// Set the attributes that changed to the model.
			// This will fire a call to `this.render` because of the `this.listenTo` line of code in the `initialize` function.
			this.model.set(attributes);
			// Toggle the button from "Done" to "Edit".
			this.$el.find('.done-edit-btn')
				.val('Edit')
				.removeClass('done-edit-btn')
				.addClass('edit-user-btn');
		}
	});
	
	// The view for the whole table, backed by a collection of users.
	// Note that events and render at the row level will be handled by the previously defined `UserRowView`.
	var UserTableView = Backbone.View.extend({
		// This `el` is a reference to the table already rendered by Thymeleaf.
		el: $("#user-table-template"),
		// A counter for the sequence of `id`s for new `User`s.
		key: -1,
		// Listen to clicks on the "Add" button.
		events : {
			'click #add-new-user-btn': 'createOnClickAddButton',
		},
		// Called when creating _the_ instance of this view.
		initialize: function() {
			// Ah ha! Collect data of existing users render by Thymeleaf just before listening to "add" events on the users
			this.addUsersFromTable();
			// Now listen to users being added to the collection
			this.listenTo(users, 'add', this.addOne);
		},
		// Collect date from the HTML table rendered by Thymeleaf and create a model and a view for each row
		addUsersFromTable: function() {
			this.$el.find('tbody > tr').each(function(index, row) {
				var id = parseInt($(row).find('td:nth-child(1)').text());
				var name = $(row).find('td:nth-child(2)').text();
				var email = $(row).find('td:nth-child(3)').text();
				// Create a new model object from the data harvested from the HTML table and add it to Backbones's collection
				var user = users.create({
					name : name,
					email: email,
					id: id,
					key: id
				});
				// Create a view for each model. NOTE that the this view will not be rendered and added to the table yet.
				var view = new UserRowView({
					model : user,
					el: row
				});
			});
		},
		// Add a new user to the collection with data from the table header input fields
		createOnClickAddButton : function(e) {
			var name = $('#new-user-form input[name=name]').val();
			var email = $('#new-user-form input[name=email]').val();
			users.create({
				name : name,
				email: email,
				id: app.key,
				key: app.key
			});
		},
		// Create a new view for a new user being added to the collection of users
		addOne : function(user) {
			var view = new UserRowView({
				model : user
			});
			// Here's where UserTableView delegates to UserRowView to render a new User.
			view.render();
			// And appends the rendered view element `el` to its own `el`.
			this.$el.append(view.el);
			// Keep counting down for new ids.
			this.key--;
		}
	});
	
	// Instantiate the app!
	var app = new UserTableView;
	
});