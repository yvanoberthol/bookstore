$(document).ready(function () {
    $('.cartItemQty').change(function (){
        var id = this.id;
        console.log("test"+id);
        $('#update-item-'+id).css('display', 'inline-block');

    });

    $('#theSameAsShippingAddress').on('click',checkBillingAddress);

    $('#confirmPassword').keyup(checkPasswordMatch);
    $('#newPassword').keyup(checkPasswordMatch);

    /*$('.collapse').on('shown.bs.collapse', function () {
        $(this).prev().addClass('active');
    });

    $('.collapse').on('hidden.bs.collapse', function () {
        $(this).prev().removeClass('active');
    });*/

});

function checkBillingAddress() {
    if ($('#theSameAsShippingAddress').is(":checked")){
        $('.billingAddress').prop("disabled", true);
    }else{
        $('.billingAddress').prop("disabled", false);
    }
}

function checkPasswordMatch() {
    var password = $('#newPassword').val();
    var confirmPassword = $('#confirmPassword').val();
    
    if (password==="" && confirmPassword === ""){
        $('#checkPasswordMatch').html("").removeClass("fa fa-check fa-close");
        $('#updateUserInfoButton').prop('disabled',false);
    }else {
        if (password !== confirmPassword){
            $('#checkPasswordMatch').html("Password do not match").css('color','red')
                .removeClass("fa fa-check").addClass("fa fa-close");
            $('#updateUserInfoButton').prop('disabled',true);
        }else{
            $('#checkPasswordMatch').html("Password match").css('color','green')
                .removeClass("fa fa-close").addClass("fa fa-check");
            $('#updateUserInfoButton').prop('disabled',false);
        }
    }
    
}