
// handle mobile layout ==============

var SIDEBAR_MOBILE = 0;
var SIDEBAR_FULL = 1;

var sidebarContent;
var sidebarPrevState = SIDEBAR_FULL;

$(window).resize(function() {
    var width = $(this).width();
    if (width < 768 && sidebarPrevState == SIDEBAR_FULL) {
        sidebarPrevState = SIDEBAR_MOBILE;
        sidebarContent.removeClass('in');
    } else if (width >= 768 && sidebarPrevState == SIDEBAR_MOBILE) {
        sidebarPrevState = SIDEBAR_FULL;
        sidebarContent.addClass('in');
    }
});

$(function() {
    sidebarContent = $('#sidebar-content');
    $(window).trigger('resize');

    $('.btn-collapse').click(function() {
        icon = $(this).children('.glyphicon');
        icon.toggleClass('glyphicon-chevron-down');
        icon.toggleClass('glyphicon-chevron-up');
    });
});