!function ($) {

  $(function(){
    
    $("a[data-toggle=popover]")
    .popover()
    .click(function(e) {
      e.preventDefault()
    })
  })

}(window.jQuery)