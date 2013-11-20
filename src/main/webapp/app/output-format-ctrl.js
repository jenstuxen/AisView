function dndCtrl($scope,UrlService) {
    //inspiration from:
    //http://www.smartjava.org/content/drag-and-drop-angularjs-using-jquery-ui
    //Control of header included in query
    $scope.headerChecked = false;

    //Control of format
    $scope.format = '';

    // Array of table separators: source data.js
    $scope.tableSeparators = tableSeparators;

    //Start up table separator: source data.js
    $scope.tableSeparator = tableSeparator;

    //Start filter on notIncludedList
    $scope.filterCategory = 'all';

    //Array of objects not included in query: source data.js
    $scope.notIncluded = notIncluded;

 	//Array of objects included in query (starts empty)
    $scope.included = [];

    $scope.includedEmpty = function() {
        return $scope.included.length == 0;
    }

    $scope.notIncludedEmpty = function() {
        return $scope.notIncluded.length == 0;
    }

    //Make example of a header line
    $scope.headerPreview = function() {
        //filter tableSeparators array to find separator character
        var filteredArray = $scope.tableSeparators.filter(function (element) {
            return element.ID == $scope.tableSeparator.sep;
        });

        var breadcrumb = '';
        angular.forEach($scope.included, function(item) {
            breadcrumb+=item.queryName+filteredArray[0].Value;
        });
        //always remove last separator from string
        breadcrumb=breadcrumb.slice(0, -1);
        return breadcrumb;
    };

    //Make the example of query line
    $scope.exPreview = function() {
        //filter tableSeparators array to find separator character
        var filteredArray = $scope.tableSeparators.filter(function (element) {
            return element.ID == $scope.tableSeparator.sep;
        });

        var breadcrumb = '';
        angular.forEach($scope.included, function(item) {
            breadcrumb+=item.ex+filteredArray[0].Value;
        });
        //always remove last separator from string
        breadcrumb=breadcrumb.slice(0, -1);
        return breadcrumb;

    };

    //If format is changed push to service
    $scope.$watch('format', function() {
        var tables = '';
        var separator = '';
        var header = '';

        //append all tables if format is 'table' else no tables
        if($scope.format=='') {
            tables = '';
            separator = '';
        }
        else {
            //only append if included tables list is not empty
            if($scope.included.length!=0){
                tables='tables=';
                angular.forEach($scope.included, function(includedItem) {
                    tables+=includedItem.queryName+',';
                });

                //delete , with & at end of string
                tables=tables.replace(/^,|,$/g,'&');

                separator='separator='+$scope.tableSeparator.sep+'&';
            }
        }

        //header control
        if($scope.headerChecked && $scope.format!='') header = 'header=true&';
        else header = '';

        //Send to service
        UrlService.setTables(tables);
        UrlService.setSeparator(separator);
        UrlService.setHeader(header);
    }, true);


    //If included array are changed push to service
    $scope.$watch('included', function() {
        var tables = '';
        var separator = '';

        //append all tables if format is 'table' else no tables
        if($scope.format=='') {
            tables = '';
            separator = '';
        }
        else {
            //only append if included tables list is not empty
            if($scope.included.length!=0){
                tables='tables=';
                angular.forEach($scope.included, function(includedItem) {
                    tables+=includedItem.queryName+',';
                });

                //delete , with & at end of string
                tables=tables.replace(/^,|,$/g,'&');

                separator='separator='+$scope.tableSeparator.sep+'&';
            }
        }

        //Send to service
        UrlService.setTables(tables);
        UrlService.setSeparator(separator);
    }, true);

    //If tableSeparator are changed push to service
    $scope.$watch('tableSeparator', function() {
        var separator = '';

        if($scope.format=='') separator = '';
        else {
            //only append if included tables list is not empty
            if($scope.included.length!=0) separator='separator='+$scope.tableSeparator.sep+'&';
        }
        //Send to service
        UrlService.setSeparator(separator);
    }, true); // <-- objectEquality

    //If headerChecked are changed push to service
    $scope.$watch('headerChecked', function() {

        var header = '';
        if($scope.headerChecked) header = 'header=true&';
            else header = '';

        //Send to service
        UrlService.setHeader(header);
    });

}