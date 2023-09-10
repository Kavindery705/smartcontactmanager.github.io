const toggleSidebar = () => {

	console.log("clicked");
    if($(".sidebar").is(":visible")){

        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");
    }
    else{
        $(".sidebar").css("display","block");
         $(".content").css("margin-left","20%");
    }

}
/*tinymce.init({
  selector: 'textarea#default-editor'
});*/
function deleteContact(cid){
				console.log("delete co");
				Swal.fire({
  title: 'Are you sure?',
  text: "You want to delete contact",
  icon: 'warning',
  showCancelButton: true,
  confirmButtonColor: '#3085d6',
  cancelButtonColor: '#d33',
  confirmButtonText: 'Yes, delete it!'
}).then((result) => {
  if (result.isConfirmed) {
   
    window.location="/user/delete/"+cid;
  }
})
}
function deleteProfile(cid){
				console.log("delete co");
				Swal.fire({
  title: 'Are you sure?',
  text: "You want to delete your profile permanently",
  icon: 'warning',
  showCancelButton: true,
  confirmButtonColor: '#3085d6',
  cancelButtonColor: '#d33',
  confirmButtonText: 'Yes, delete it!'
}).then((result) => {
  if (result.isConfirmed) {
   
    window.location="/user/profile/delete/"+cid;
  }
})
}

const search = () =>{
	console.log("searcg")
	
 let query = $("#search-input").val();
  if(query==''){
    $(".search-result").hide();
  }
  else{
    let url = `http://localhost:8084/search/${query}`;
    fetch(url).then((response)=>{
      return response.json();
    }).then((data)=>{
	console.log(data)
  let text = `<div class='list-group'>`;
      data.forEach((contact) => {
        text+=`<a href="/user/contact/${contact.cid}" class="list-group-item list-group-action"> ${contact.name}</a>`;
      });

  text +=`<div>`;
  $(".search-result").html(text);
  $(".search-result").show();
    });
  }

};

//first request- tot server to create order

const paymentStart=()=>{

 let amount = $("#payment_field").val();
 console.log(amount);
	if(amount=='' || amount == null){
			Swal.fire(
			  'Amount is required',
			  'You clicked the button!',
			  'error'
			)
	return;
	}
//we will use ajax
$.ajax(
  {
    url:'/user/create_order',
    data :JSON.stringify({amount:amount,info:'order_request'}),
    contentType:'application/json',
    type:'POST',
    dataType:'json',
    success:function(response){
      console.log(response);
      if(response.status=='created'){
		  //open payment form
		  
		  let option = {
			  key:'rzp_test_j5tVXEsrEmOdcZ',
			  amount:response.amount,
			  currency:'INR',
			  name:'Smart Contact Manager',
			  description:'Donation',
			  image:'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSrYLISGAGwLtVe8fMi8iK84uKUbw6thY_3Yg&usqp=CAU',
			  order_id:response.id,
			  handler:function(response){
				  console.log(response.razorpay_payment_id);
				  console.log(response.razorpay_order_id);
				  console.log(response.razorpay_signature);
				  console.log('payment successfull');
				  
				  updatePaymentOnServer(
					  response.razorpay_payment_id,
					  response.razorpay_order_id,
					  "paid"
				  );
							
			  },
			 prefill: {
			 name: "",
			 email: "",
			 contact: ""
			 },
			notes: {
			 address: "SCM Office"
			 },
			 theme: {
			 color: "#3399cc"
			 }
		  };
		  let rzp = new Razorpay(option);
					  rzp.on('payment.failed', function (response){
			 console.log(response.error.code);
			 console.log(response.error.description);
			 console.log(response.error.source);
			 console.log(response.error.step);
			 console.log(response.error.reason);
			 console.log(response.error.metadata.order_id);
			 console.log(response.error.metadata.payment_id);
						Swal.fire(
			  'Oops payment failed',
			  'You clicked the button!',
			  'error'
			)
			});
		  rzp.open();
		  
	  }
    },
    error:function(error){
		console.log(error);
     	Swal.fire(
			  'something went wrong',
			  'You clicked the button!',
			  'error'
			)
    }
  }
);

};

function updatePaymentOnServer(payment_id,order_id,status){
	$.ajax(
  {
    url:'/user/update_order',
    data :JSON.stringify({payment_id:payment_id,order_id:order_id,status:status}),
    contentType:'application/json',
    type:'POST',
    dataType:'json',
    success:function(response){
		  Swal.fire(
			  'Payment done',
			  'Congrats!!',
			  'success'
			)
	},
    error:function(error){
		  Swal.fire(
			  'failed!!',
			  'Your payment is successful,but we did not get on server , we will contact you as soon as possible',
			  'success'
			)
	},
    });
}
