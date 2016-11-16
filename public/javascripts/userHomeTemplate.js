  $('.open-delete-modal').click(function() {
    var giftid = $(this).data('id');
    var action = $(this).data('action');
    $(".modal-footer #delete-event-form").attr("action", "/" + action + "/" + giftid);
    $('#delete-modal').modal('show');
  });


  $('.open-gift-status-modal').click(function() {
    var giftid = $(this).data('id');
    $("#update-gift-status-" + giftid + "-form").attr("action", "/updateGiftStatus/" + giftid);
    $('#update-gift-status-modal-' + giftid).modal('show');
  });

  $('.dropdown-toggle').dropdown()

  $(document).ready(function() {
    $('.button-tooltip').tooltip();
  });