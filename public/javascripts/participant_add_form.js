
$('.add-participant-btn').on('click', function(e) {
  $.ajax({
    url: '/addParticipant',
    type: 'GET',
    contentType: "application/x-www-form-urlencoded",
    data: $('.add-participant-form').serializeArray(),
    success: function( response, textStatus, xhr ) {
      $('#participants-table').html(response)
    },
    error: function(jqXHR, textStatus, errorThrown) {
      if(jqXHR.status == 400) {
        $('.add-participant-form').html(jqXHR.responseText)
      }
    }
  });
})