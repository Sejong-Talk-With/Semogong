
function alert_error(){
    alert("로그인 후 댓글을 달 수 있습니다!");
}

function login_check() {
    alert("로그인 후 서비스를 이용할 수 있습니다!");
}

function state_error(){
    alert("현재 상태를 확인해주세요!")
}


function check_end(memberId) {


    $.ajax({
        url: '/members/times/' + memberId,
        type: "GET"
    })
        .done(function (response) {
            var currTotal = response.hour * 60 + response.min;
            if (currTotal < memberTodayGoalTotal) {
                var timeDiff = memberTodayGoalTotal - currTotal;
                var check = confirm("아직 목표 시간에 도달하지 못하셨습니다. (" + Math.floor(timeDiff / 60) + "시간 " + timeDiff % 60 + "분 부족)\n사용을 종료하시겠습니까?");
                if (check) {
                    window.location.href = "/end";
                }
            } else {
                window.location.href = "/end";
            }
        })
}

function delete_check(postId, commentId) {

    var deleteForm = {
        postId : postId,
        commentId : commentId
    };

    var check = confirm("정말 삭제하시겠습니까?");

    if (check){ //확인
        $.ajax({
            url: '/comment/delete/' + commentId,
            type: "DELETE",
            data: deleteForm
        })
            .done(function (fragment) {
                $('#commentPart' + postId).replaceWith(fragment);
                var obj = document.getElementById("commentPart");
                obj.setAttribute("id", "commentPart"+postId.toString());

                var count = 1;
                var obj2 = document.getElementById("commentCount"+ postId);
                if (obj2) {
                    count = parseInt(document.getElementById("commentCount"+ postId).innerText) - 1;
                    $('#commentCount'+ postId).replaceWith(
                        '<p class="text-muted mb-0" style="font-size: 0.7em; margin-left: 3px" id="commentCount'+ postId + '">'+ count +'</p>'
                    );
                }
                if (recentPostId == postId){
                    count = parseInt(document.getElementById("recent_commentCount"+ postId).innerText) - 1;
                    $('#recent_commentCount'+ postId).replaceWith(
                        '<p class="text-muted mb-0" style="font-size: 0.7em; margin-left: 3px" id="recent_commentCount'+ recentPostId + '">'+ count +'</p>'
                    );
                }
            });
    }else{   //취소
        return false;
    }
}



function enterKey(id) {
    if (window.event.keyCode == 13) {
        $("#comment_submit_btn" + id).click();
    }

}

function editEnterKey(id) {
    if (window.event.keyCode == 13) {
        $("#edit_comment_submit_btn" + id).click();
    }

}


function submit_comment(id) {

    var commentForm = {
        postId : id,
        comment : $("#comment_content"+id).val()
    };

    $.ajax({
        url: '/comment/api/new/' + id,
        type: "POST",
        data: commentForm,
    })
        .done(function (fragment) {
            $('#commentPart' + id).replaceWith(fragment);
            var commentPart = document.getElementById("commentPart");
            commentPart.setAttribute("id", "commentPart"+id.toString());

            var count = 1;
            var commentCount = document.getElementById("commentCount" + id);
            var recentCommentCount = document.getElementById("recent_commentCount" + id);
            var post = document.getElementById("post" + id);
            if (post) {
                if (commentCount) {
                    count = parseInt(commentCount.innerText) + 1;
                    $('#commentCount' + id).replaceWith(
                        '<p class="text-muted mb-0" style="font-size: 0.7em; margin-left: 3px" id="commentCount' + id + '">' + count + '</p>'
                    );
                    if (recentPostId == id) {
                        $('#recent_commentCount' + id).replaceWith(
                            '<p class="text-muted mb-0" style="font-size: 0.7em; margin-left: 3px" id="recent_commentCount' + recentPostId + '">' + count + '</p>'
                        );
                    }
                } else {
                    $('#commentDiv' + id).append(
                        '<img src="/images/comment.png" width="14px" height="14px"/> ' +
                        '<p class="text-muted mb-0" style="font-size: 0.7em; margin-left: 3px"' +
                        'id="commentCount' + id + '"> 1 </p>'
                    );
                    if (recentPostId == id) {
                        $('#recent_commentDiv' + id).append(
                            '<img src="/images/comment.png" width="14px" height="14px"/> ' +
                            '<p class="text-muted mb-0" style="font-size: 0.7em; margin-left: 3px"' +
                            'id="recent_commentCount' + id + '"> 1 </p>'
                        );
                    }
                }
            } else {
                if (recentCommentCount) {
                    count = parseInt(recentCommentCount.innerText) + 1;
                    $('#recent_commentCount' + id).replaceWith(
                        '<p class="text-muted mb-0" style="font-size: 0.7em; margin-left: 3px" id="recent_commentCount' + recentPostId + '">' + count + '</p>'
                    );
                } else {
                    $('#recent_commentDiv' + id).append(
                        '<img src="/images/comment.png" width="14px" height="14px"/> ' +
                        '<p class="text-muted mb-0" style="font-size: 0.7em; margin-left: 3px"' +
                        'id="recent_commentCount' + id + '"> 1 </p>'
                    );
                }
            }

        });
}

function edit_comment_form(id, commentId) {
    $.ajax({
        url: '/comment/api/edit/' + id +'/' + commentId,
        type: "GET",
    })
        .done(function (fragment) {
            $('#comment'+commentId).replaceWith(fragment);
            var comment = document.getElementById("comment");
            comment.setAttribute("id", "comment" + commentId.toString());
        })
}

function edit_comment(id, commentId) {

    var commentForm = {
        postId : id,
        comment : $("#edit_comment_content"+commentId).val()
    };

    $.ajax({
        url: '/comment/api/edit/' + id +'/' + commentId,
        type: "POST",
        data: commentForm,
    })
        .done(function (fragment) {
            $('#commentPart' + id).replaceWith(fragment);
            var commentPart = document.getElementById("commentPart");
            commentPart.setAttribute("id", "commentPart"+id.toString());
        });
}

function edit_post(id) {
    $.ajax({
        url: '/posts/edit/' + id,
        type: "GET",
    })
        .done(function (fragment) {
            $('#postModal_content' + id).replaceWith(fragment);
            simplemde = new SimpleMDE({element: document.getElementById("content"),spellChecker: false});
            document.getElementById("postEdit_container").scrollIntoView();
        });
}

function post_delete_check(id) {
    var check = confirm("정말 삭제하시겠습니까?");
    if (check){    //확인
        alert("삭제가 완료 되었습니다.")
        $.ajax({
            url: '/posts/delete/' + id,
            type: "DELETE"
        })
            .done(function () {
                location.reload();
            })
    }else{   //취소
        return false;
    }
}

function post_edit(id) {
    var formData = $("#formPost").serializeArray();
    formData[formData.length - 1].value = simplemde.value(); // content

    var times = [];
    var idx = 0
    var result_hour = 0;
    var result_min = 0;
    var reg = /^\d{1,2}:\d{1,2}$/;
    let todayTime = new Date();
    let hoursToday = todayTime.getHours(); // 시
    let minutesToday = todayTime.getMinutes();  // 분
    let totalTimeToday = hoursToday*60 + minutesToday;

    for (var step = 1; step < formData.length-3; idx++, step++) {
        if (formData[step].value === "") continue;
        times[idx] = formData[step];

        if (!reg.test(times[idx].value)) {
            alert('잘못된 형식의 시간이 입력되었습니다.\n올바른 형식: "시간:분" ex) 17:20');
            var temp_times_id = "times" + idx;
            var timeHtml = document.getElementById(temp_times_id);
            timeHtml.setAttribute('class', 'form-control is-invalid');
            timeHtml.setAttribute('style', 'background-image: none; padding: 6px;');
            document.getElementById("postEdit_container").scrollIntoView();
            return;
        }
        var curr = times[idx].value.split(':')
        var currTime = parseInt(curr[0])*60 + parseInt(curr[1])
        if (currTime > totalTimeToday) {
            alert("현재시간을 초과한 시간값이 있습니다.");
            var curr_times_id = "times" + idx;
            var timeHtmlCurr = document.getElementById(curr_times_id);
            timeHtmlCurr.setAttribute('class', 'form-control is-invalid');
            timeHtmlCurr.setAttribute('style', 'background-image: none; padding: 6px;');
            document.getElementById("postEdit_container").scrollIntoView();
            return;
        }
        if (idx !== 0){
            var before = times[idx-1].value.split(':');
            var beforeTime = parseInt(before[0])*60 + parseInt(before[1]);
            if (beforeTime > currTime) {
                alert("이후 시간이 이전시간보다 작습니다. 수정해주세요!");
                var timeAfter = "times" + idx;
                var timeBefore = "times" + (idx-1);
                var timeHtmlAfter = document.getElementById(timeAfter);
                var timeHtmlBefore = document.getElementById(timeBefore);
                timeHtmlAfter.setAttribute('class', 'form-control is-invalid');
                timeHtmlAfter.setAttribute('style', 'background-image: none; padding: 6px;');
                timeHtmlBefore.setAttribute('class', 'form-control is-invalid');
                timeHtmlBefore.setAttribute('style', 'background-image: none; padding: 6px;');
                document.getElementById("postEdit_container").scrollIntoView();
                return;
            }
        }


    }



    if (focuedPostId == id) {
        if (times.length % 2 == 0) {
            var total1 = 0;

            for (var i = 1; i < times.length; i += 2) {
                var ends1 = times[i].value.split(":");
                var starts1 = times[i - 1].value.split(":");
                var endHour1 = parseInt(ends1[0]);
                if (0 <= endHour1 && endHour1 < 4) {
                    endHour1 += 24;
                }
                var endMin1 = parseInt(ends1[1]);
                var end1 = endHour1 * 60 + endMin1;

                var startHour1 = parseInt(starts1[0]);
                if (0 <= startHour1 && startHour1 < 4) {
                    startHour1 += 24;
                }
                var startMin1 = parseInt(starts1[1]);
                var start1 = startHour1 * 60 + startMin1;

                total1 += (end1 - start1);

            }


            result_hour = Math.floor(total1 / 60);
            result_min = total1 % 60;

        } else {
            var total2 = 0;
            for (var i = 1; i < times.length; i += 2) {
                var ends = times[i].value.split(":");
                var starts = times[i - 1].value.split(":");
                var endHour = parseInt(ends[0]);
                if (0 <= endHour && endHour < 4) {
                    endHour += 24;
                }
                var endMin = parseInt(ends[1]);
                var end = endHour * 60 + endMin;

                var startHour = parseInt(starts[0]);
                if (0 <= startHour && startHour < 4) {
                    startHour += 24;
                }
                var startMin = parseInt(starts[1]);
                var start = startHour * 60 + startMin;

                total2 += (end - start);
            }

            var last_starts = times[times.length-1].value.split(":");
            var today = new Date();

            var sHour = parseInt(last_starts[0]);
            if (0 <= sHour && sHour < 4) {
                sHour += 24;
            }
            var sMin = parseInt(last_starts[1]);
            var last_start = sHour * 60 + sMin;

            var hours = parseInt(('0' + today.getHours()).slice(-2));
            if (0 <= hours && hours < 4) {
                hours += 24;
            }
            var minutes = parseInt(('0' + today.getMinutes()).slice(-2));
            var now_end = hours * 60 + minutes;

            total2 += (now_end - last_start);

            result_hour = Math.floor(total2 / 60);
            result_min = total2 % 60;

        }
    }


    var title = formData[formData.length - 3].value;
    var introduce = formData[formData.length - 2].value;
    var content = formData[formData.length - 1].value;



    if (formData[formData.length - 3].value == '') { // title
        alert("제목을 입력해주세요!");
        var titleHtml = document.getElementById("title");
        titleHtml.setAttribute('class','form-control is-invalid');
        document.getElementById("times0").scrollIntoView();
    } else if (state == 'STUDYING' & times_len % 2 == 0) {
        alert("[STUDYING] 상태입니다. 시간 개수를 확인해주세요!");
        var times_id = "times" + (times_len-1);
        var timeInput = document.getElementById(times_id);
        timeInput.setAttribute('class','form-control is-invalid');
        document.getElementById("postEdit_container").scrollIntoView();
    } else if ((state == 'BREAKING' || state == 'END') & times_len % 2 != 0) {
        alert("[BREAKING] 혹은 [END] 상태입니다. 시간 개수를 확인해주세요!");
        var times_id = "times" + (times_len-1);
        var titleHtml = document.getElementById(times_id);
        titleHtml.setAttribute('class', 'form-control is-invalid');
        document.getElementById("postEdit_container").scrollIntoView();
    } else {
        $.ajax({
            url: '/posts/edit/' + id,
            type: "POST",
            data: formData
        })
            .done(function (fragment) {
                $('#postEdit_container').replaceWith(fragment);
                var postModal_content = document.getElementById("postModal_content");
                postModal_content.setAttribute("id", "postModal_content" + id.toString());


                var obj = document.getElementById("post" + id);
                if (obj) {
                    $('#post_title' + id).replaceWith(
                        '<p id="post_title' + id + '" class="card-title h5 d-sm-flex align-items-center text-truncate fw-bold" style="font-size: 20px;  color: gray; height: 30px;">' + title + '</p>'
                    );
                    $('#post_introduce' + id).replaceWith(
                        '<p id="post_introduce' + id + '" class="small" style="margin: 3px">' + introduce + '</p>'
                    )
                    $('#post_times' + id).replaceWith(
                        '<div id="post_times' + id + '" class="card-text overflow-hidden d-sm-flex" style="margin: 2px 0px 0px 0px; max-width: 100%;"></div>'
                    )
                    var cnt = 0;
                    for (var i = 1; i < times.length; i += 2, cnt++) {
                        if (cnt % 2 != 0) {
                            $('#post_times' + id).append(
                                '<p class="badge" style="margin: 3px; background-color:#8AA6A3;">' +
                                times[i - 1].value + '&nbsp;~&nbsp;' + times[i].value
                                + '</p>'
                            );
                        } else {
                            $('#post_times' + id).append(
                                '<p class="badge" style="margin: 3px; background-color:#A1A185;">' +
                                times[i - 1].value + '&nbsp;~&nbsp;' + times[i].value
                                + '</p>'
                            );
                        }
                    }
                    if (times.length % 2 == 1) {
                        if ((times.length - 1) % 4 == 0) {
                            $('#post_times' + id).append(
                                '<p class="badge" style="margin: 3px; background-color:#A1A185;">' +
                                times[times.length - 1].value + '&nbsp;~'
                                + '</p>'
                            );
                        } else {
                            $('#post_times' + id).append(
                                '<p class="badge" style="margin: 3px; background-color:#8AA6A3;">' +
                                times[times.length - 1].value + '&nbsp;~'
                                + '</p>'
                            );
                        }
                    }
                }

                if (focuedPostId == id) {
                    $('#member_time').replaceWith(
                        '<div id="member_time"> 오늘의 학습 시간 : ' + result_hour + '시간 ' + result_min + '분 </div>'
                    )
                }

                if (recentPostId == id) {
                    $('#recent_post_title' + id).replaceWith(
                        '<h2 id="recent_post_title' + id + '" class="card-title h5 d-sm-flex align-items-center text-truncate fw-bold" style="font-size: 20px; color: gray; height: 40px;">' + title + '</h2>'
                    );
                    $('#recent_post_introduce' + id).replaceWith(
                        '<p id="recent_post_introduce' + id + '" class="small" style="margin: 3px">' + introduce + '</p>'
                    );
                    $('#recent_post_times' + id).replaceWith(
                        '<div id="recent_post_times' + id + '" class="card-text overflow-hidden d-sm-flex" style="margin: 2px 0px 0px 0px; max-width: 100%;"></div>'
                    );
                    var cnt1 = 0;
                    for (var j = 1; j < times.length; j += 2, cnt1++) {
                        if (cnt1 % 2 != 0) {
                            $('#recent_post_times' + id).append(
                                '<p class="badge" style="margin: 3px; background-color:#8AA6A3;">' +
                                times[j - 1].value + '&nbsp;~&nbsp;' + times[j].value
                                + '</p>'
                            );
                        } else {
                            $('#recent_post_times' + id).append(
                                '<p class="badge" style="margin: 3px; background-color:#A1A185;">' +
                                times[j - 1].value + '&nbsp;~&nbsp;' + times[j].value
                                + '</p>'
                            );
                        }
                    }
                    if (times.length % 2 == 1) {
                        if ((times.length - 1) % 4 == 0) {
                            $('#recent_post_times' + id).append(
                                '<p class="badge" style="margin: 3px; background-color:#A1A185;">' +
                                times[times.length - 1].value + '&nbsp;~'
                                + '</p>'
                            );
                        } else {
                            $('#recent_post_times' + id).append(
                                '<p class="badge" style="margin: 3px; background-color:#8AA6A3;">' +
                                times[times.length - 1].value + '&nbsp;~'
                                + '</p>'
                            );
                        }
                    }
                }

                document.getElementById("postModal_content" + id).scrollIntoView();
            });
    }


}

function image_upload(id){

    var formData = new FormData();
    var image = $("#file")[0].files[0];


    formData.append( "file", image);

    if(image == null){
        alert("파일을 선택해주세요");
        return;
    }


    $.ajax({
        url : "/posts/edit/"+id+"/img"
        , type : "POST"
        , processData : false
        , contentType : false
        , data : formData
        , success:function(response) {
            alert("이미지 변경이 완료 되었습니다!");
            var imgSrc = response.imgSrc;
            var imgName = response.imgName;
            $("#post_img"+id).replaceWith(
                '<img id="post_img'+ id +'" class="img-thumbnail"  height="150px" width="270px" src="'+ imgSrc +'">'
            )
            $('#post_img_name'+id).replaceWith(
                '<div id="post_img_name'+ id + '" style="display: inline-block">' +
                '( 현재 이미지 : <p class="mb-0" style="display: inline-block; font-weight: bold;">'+imgName+'</p>&nbsp;)' +
                '</div>'
            )

            $('#home_post_img'+id).replaceWith(
                '<img id="home_post_img'+ id +'" class="card-img-top d-sm-flex justify-content-center align-items-center"' +
                ' src="'+imgSrc+'"alt="..." style="border: solid lightgray 1px; border: 0px;">'
            )

            if (recentPostId == id) {
                $('#recent_home_post_img'+id).replaceWith(
                    '<img id="recent_home_post_img'+ id +'" class="card-img-top d-sm-flex justify-content-center align-items-center"' +
                    ' src="'+imgSrc+'"alt="..." style="border: solid lightgray 1px; border: 0px;">'
                )

            }



        }
        ,error: function ()
        {
            alert("업로드 실패, 잠시 후 다시 시도해주세요!");
        }
    });
}

function reload_times(memberId) {
    $.ajax({
        url: '/members/times/' + memberId,
        type: "GET"
    })
        .done(function (response) {
            $('#member_time').replaceWith(
                '<div id="member_time"> 오늘의 학습 시간 : ' + response.hour + '시간 '+ response.min +'분 </div>'
            )
        })
}

var rotate = document.getElementById('reload')

if (rotate) {
    rotate.addEventListener('click', function() {
        this.className = 'reload';
        window.setTimeout(function() {
            rotate.className = ''
        }, 1000);
    }, false);
}
