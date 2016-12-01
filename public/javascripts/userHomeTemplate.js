  $('.open-delete-modal').click(function() {
    if($(this).attr("disabled") !== undefined) {
        return;
    }
    var giftid = $(this).data('id');
    var action = $(this).data('action');
    $(".modal-footer #delete-event-form").attr("action", "/" + action + "/" + giftid);
    $('#delete-modal').modal('show');
  });


  $('.open-gift-status-modal').click(function() {
    if($(this).attr("disabled") !== undefined) {
        return;
    }
    var giftid = $(this).data('id');
    $("#update-gift-status-" + giftid + "-form").attr("action", "/updateGiftStatus/" + giftid);
    $('#update-gift-status-modal-' + giftid).modal('show');
  });

  $('.open-gift-comment-modal').click(function() {
    if($(this).attr("disabled") !== undefined) {
        return;
    }
    var giftid = $(this).data('id');
    $(".comments").empty();
    $.ajax({
        type : 'GET',
        url : "/gift/" + giftid + "/comment",
        success : function(data) {
            var commentTable = ""
            for(var i=0;i < data.length; i++){
                commentTable +="<div class='media'>";
                commentTable +="<p class='pull-right'><small>" + data[i].creationDate + "</small></p>";
                commentTable +="<div class='media-body'>";
                commentTable +="<h4 class='media-heading user_name'>" + data[i].username + "</h4>";
                commentTable += data[i].content;
                commentTable +="</div>";
                commentTable +="</div>";
            }

            $(".comments").append(commentTable);
        },
        error : function(data) {
            console.log('Error dude:' + data);
        }
    });
    $('#comment-gift-status-modal-' + giftid).modal('show');
  });

  $(".gift-comment-form").submit(function(e) {
      var giftid = $(this).data('id');
      $.ajax({
             type: "POST",
             url: "/gift/" + giftid + "/comment",
             data: $(this).serialize(),// serializes the form's elements.
             success: function(data)
             {
                var commentTable = ""
                for(var i=0;i < data.length; i++){
                    commentTable +="<div class='media'>";
                    commentTable +="<p class='pull-right'><small>" + data[i].creationDate + "</small></p>";
                    commentTable +="<div class='media-body'>";
                    commentTable +="<h4 class='media-heading user_name'>" + data[i].username + "</h4>";
                    commentTable += data[i].content;
                    commentTable +="</div>";
                    commentTable +="</div>";
                }
                $(".comments").empty();
                $(".comments").append(commentTable);
             }
           });
      $(this).trigger('reset');
      e.preventDefault(); // avoid to execute the actual submit of the form.
  });

  $('.dropdown-toggle').dropdown()

  $(document).ready(function() {
    $('.button-tooltip').tooltip();
  });