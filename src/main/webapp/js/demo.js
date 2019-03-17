$(function(){

  var layoutPadding = 10;
  var aniDur = 200;
  var easing = 'linear';

  var cy;

  var graphP = $.ajax({
    url: './data.json',
    type: 'GET',
    dataType: 'json'
  });

  var styleP = $.ajax({
    url: './style.cycss',
    type: 'GET',
    dataType: 'text'
  });

  var infoTemplate = Handlebars.compile([
    '<p class="ac-name">{{name}}</p>',
    '<p class="ac-node-type"><i class="fa fa-info-circle"></i> {{NodeTypeFormatted}} {{#if Type}}({{Type}}){{/if}}</p>',
    '{{#if Milk}}<p class="ac-milk"><i class="fa fa-angle-double-right"></i> {{Milk}}</p>{{/if}}',
    '<p class="ac-more"><i class="fa fa-external-link"></i> <a target="_blank" href="https://dicionariocriativo.com.br/{{name}}">More information</a></p>'
  ].join(''));

  Promise.all([ graphP, styleP ]).then(initCy);

  var allNodes = null;
  var allEles = null;
  var lastHighlighted = null;
  var lastUnhighlighted = null;

  function getFadePromise( ele, opacity ){
    return ele.animation({
      style: { 'opacity': opacity },
      duration: aniDur
    }).play().promise();
  };

  var restoreElesPositions = function( nhood ){
    return Promise.all( nhood.map(function( ele ){
      var p = ele.data('orgPos');

      return ele.animation({
        position: { x: p.x, y: p.y },
        duration: aniDur,
        easing: easing
      }).play().promise();
    }) );
  };

  function highlight( node ){
    var oldNhood = lastHighlighted;

    var nhood = lastHighlighted = node.closedNeighborhood();
    var others = lastUnhighlighted = cy.elements().not( nhood );

    var reset = function(){
      cy.batch(function(){
        others.addClass('hidden');
        nhood.removeClass('hidden');

        allEles.removeClass('faded highlighted');

        nhood.addClass('highlighted');

        others.nodes().forEach(function(n){
          var p = n.data('orgPos');

          n.position({ x: p.x, y: p.y });
        });
      });

      return Promise.resolve().then(function(){
        if( isDirty() ){
          return fit();
        } else {
          return Promise.resolve();
        };
      }).then(function(){
        return Promise.delay( aniDur );
      });
    };

    var runLayout = function(){
      var p = node.data('orgPos');

      var l = nhood.filter(':visible').makeLayout({
        name: 'concentric',
        fit: false,
        animate: true,
        animationDuration: aniDur,
        animationEasing: easing,
        boundingBox: {
          x1: p.x - 1,
          x2: p.x + 1,
          y1: p.y - 1,
          y2: p.y + 1
        },
        avoidOverlap: true,
        concentric: function( ele ){
          if( ele.same( node ) ){
            return 2;
          } else {
            return 1;
          }
        },
        levelWidth: function(){ return 1; },
        padding: layoutPadding
      });

      var promise = cy.promiseOn('layoutstop');

      l.run();

      return promise;
    };

    var fit = function(){
      return cy.animation({
        fit: {
          eles: nhood.filter(':visible'),
          padding: layoutPadding
        },
        easing: easing,
        duration: aniDur
      }).play().promise();
    };

    var showOthersFaded = function(){
      return Promise.delay( 250 ).then(function(){
        cy.batch(function(){
          others.removeClass('hidden').addClass('faded');
        });
      });
    };

    return Promise.resolve()
      .then( reset )
      .then( runLayout )
      .then( fit )
      .then( showOthersFaded )
    ;

  }

  function isDirty(){
    return lastHighlighted != null;
  }

  function clear( opts ){
    if( !isDirty() ){ return Promise.resolve(); }

    opts = $.extend({

    }, opts);

    cy.stop();
    allNodes.stop();

    var nhood = lastHighlighted;
    var others = lastUnhighlighted;

    lastHighlighted = lastUnhighlighted = null;

    var hideOthers = function(){
      return Promise.delay( 125 ).then(function(){
        others.addClass('hidden');

        return Promise.delay( 125 );
      });
    };

    var showOthers = function(){
      cy.batch(function(){
        allEles.removeClass('hidden').removeClass('faded');
      });

      return Promise.delay( aniDur );
    };

    var restorePositions = function(){
      cy.batch(function(){
        others.nodes().forEach(function( n ){
          var p = n.data('orgPos');

          n.position({ x: p.x, y: p.y });
        });
      });

      return restoreElesPositions( nhood.nodes() );
    };

    var resetHighlight = function(){
      nhood.removeClass('highlighted');
    };

    return Promise.resolve()
      .then( resetHighlight )
      .then( hideOthers )
      .then( restorePositions )
      .then( showOthers )
    ;
  }

  function showNodeInfo( node ){
    $('#info').html( infoTemplate( node.data() ) ).show();
  }

  function hideNodeInfo(){
    $('#info').hide();
  }

  function initCy( then ){
    var loading = document.getElementById('loading');
    var expJson = then[0];
    var styleJson = then[1];
    var elements = expJson.elements;

    elements.nodes.forEach(function(n){
      var data = n.data;

      data.NodeTypeFormatted = data.NodeType;

      n.data.orgPos = {
        x: n.position.x,
        y: n.position.y
      };
    });

    loading.classList.add('loaded');

    cy = window.cy = cytoscape({
      container: document.getElementById('cy'),
      layout: { name: 'preset', padding: layoutPadding },
      style: styleJson,
      elements: elements,
      motionBlur: true,
      selectionType: 'single',
      boxSelectionEnabled: false,
      autoungrabify: true
    });

    allNodes = cy.nodes();
    allEles = cy.elements();

    cy.on('free', 'node', function( e ){
      var n = e.cyTarget;
      var p = n.position();

      n.data('orgPos', {
        x: p.x,
        y: p.y
      });
    });

    cy.on('tap', function(){
      $('#search').blur();
    });

    cy.on('select unselect', 'node', _.debounce( function(e){
      var node = cy.$('node:selected');

      if( node.nonempty() ){
        showNodeInfo( node );

        Promise.resolve().then(function(){
          return highlight( node );
        });
      } else {
        hideNodeInfo();
        clear();
      }

    }, 100 ) );

  }

  var lastSearch = '';

  $('#search').typeahead({
    minLength: 2,
    highlight: true,
  },
  {
    name: 'search-dataset',
    source: function( query, cb ){
      function matches( str, q ){
        str = (str || '').toLowerCase();
        q = (q || '').toLowerCase();

        return str.match( q );
      }

      var fields = ['name', 'NodeType', 'Type'];

      function anyFieldMatches( n ){
        for( var i = 0; i < fields.length; i++ ){
          var f = fields[i];

          if( matches( n.data(f), query ) ){
            return true;
          }
        }

        return false;
      }

      function getData(n){
        var data = n.data();

        return data;
      }

      function sortByName(n1, n2){
        if( n1.data('name') < n2.data('name') ){
          return -1;
        } else if( n1.data('name') > n2.data('name') ){
          return 1;
        }

        return 0;
      }

      var res = allNodes.stdFilter( anyFieldMatches ).sort( sortByName ).map( getData );

      cb( res );
    },
    templates: {
      suggestion: infoTemplate
    }
  }).on('typeahead:selected', function(e, entry, dataset){
    var n = cy.getElementById(entry.id);

    cy.batch(function(){
      allNodes.unselect();

      n.select();
    });

    showNodeInfo( n );
  }).on('keydown keypress keyup change', _.debounce(function(e){
    var thisSearch = $('#search').val();

    if( thisSearch !== lastSearch ){
      $('.tt-dropdown-menu').scrollTop(0);

      lastSearch = thisSearch;
    }
  }, 50));

  $('#reset').on('click', function(){
    if( isDirty() ){
      clear();
    } else {
      allNodes.unselect();

      hideNodeInfo();

      cy.stop();

      cy.animation({
        fit: {
          eles: cy.elements(),
          padding: layoutPadding
        },
        duration: aniDur,
        easing: easing
      }).play();
    }
  });

  $('#filters').on('click', 'input', function(){

    var a = $('#a').is(':checked');
    var b = $('#b').is(':checked');
    var c = $('#c').is(':checked');
    var d = $('#d').is(':checked');
    var e = $('#e').is(':checked');
    var f = $('#f').is(':checked');
    var g = $('#g').is(':checked');
    var h = $('#h').is(':checked');
    var i = $('#i').is(':checked');
    var j = $('#j').is(':checked');
    var k = $('#k').is(':checked');
    var l = $('#l').is(':checked');
    var m = $('#m').is(':checked');
    var n = $('#n').is(':checked');
    var o = $('#o').is(':checked');
    var p = $('#p').is(':checked');
    var q = $('#q').is(':checked');
    var r = $('#r').is(':checked');
    var s = $('#s').is(':checked');
    var t = $('#t').is(':checked');
    var u = $('#u').is(':checked');
    var v = $('#v').is(':checked');
    var x = $('#x').is(':checked');
    var w = $('#w').is(':checked');
    var y = $('#y').is(':checked');
    var z = $('#z').is(':checked');

    cy.batch(function(){

      allNodes.forEach(function( n ){
        var type = n.data('NodeType');

        n.removeClass('filtered');

        var filter = function(){
          n.addClass('filtered');
        };

        if( type === 'Letra Inicial'){

          var cType = n.data('Type');

          if(
               (cType === 'a' && !a)
            || (cType === 'b' && !b)
            || (cType === 'c' && !c)
            || (cType === 'd' && !d)
            || (cType === 'e' && !e)
            || (cType === 'f' && !f)
            || (cType === 'g' && !g)
            || (cType === 'h' && !h)
            || (cType === 'i' && !i)
            || (cType === 'j' && !j)
            || (cType === 'k' && !k)
            || (cType === 'l' && !l)
            || (cType === 'm' && !m)
            || (cType === 'n' && !n)
            || (cType === 'o' && !o)
            || (cType === 'p' && !p)
            || (cType === 'q' && !q)
            || (cType === 'r' && !r)
            || (cType === 's' && !s)
            || (cType === 't' && !s)
            || (cType === 'u' && !u)
            || (cType === 'v' && !v)
            || (cType === 'x' && !x)
            || (cType === 'w' && !w)
            || (cType === 'y' && !y)
            || (cType === 'z' && !z)
            || (cType === undefined && !na)){
            filter();
          }
        }
      });
    });
  });

  $('#filter').qtip({
    position: {
      my: 'top center',
      at: 'bottom center',
      adjust: {
        method: 'shift'
      },
      viewport: true
    },

    show: {
      event: 'click'
    },

    hide: {
      event: 'unfocus'
    },

    style: {
      classes: 'qtip-bootstrap qtip-filters',
      tip: {
        width: 16,
        height: 8
      }
    },

    content: $('#filters')
  });

  $('#about').qtip({
    position: {
      my: 'bottom center',
      at: 'top center',
      adjust: {
        method: 'shift'
      },
      viewport: true
    },

    show: {
      event: 'click'
    },

    hide: {
      event: 'unfocus'
    },

    style: {
      classes: 'qtip-bootstrap qtip-about',
      tip: {
        width: 16,
        height: 8
      }
    },

    content: $('#about-content')
  });
});
