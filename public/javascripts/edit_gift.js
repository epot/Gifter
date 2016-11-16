
	$(document.body).on('click', '.removeUrl' ,function(){
      var urls = $(this).parents('.urls')
      $(this).parents('.url').remove()
      renumber(urls)
    })

    $('.addUrl').on('click', function() {
      var urls = $(this).parents('.urls')
      var template = $('.url_template', urls)
      template.before('<div class="clearfix url">' + template.html() + '</div>')
      renumber(urls)
    })

    // -- renumber fields

    // Rename fields to have a coherent payload like:
    //
    // urls[0]
    // urls[1]
    // ...
    //
    // This is probably not the easiest way to do it. A jQuery plugin would help.
    var renumber = function(urls) {
      $('.new-gift').each(function(i) {
        $('.url input', this).each(function(i) {
          $(this).attr('name', $(this).attr('name').replace(/urls\[.+\]/g, 'urls[' + i + ']'))
        })
      })
    }
