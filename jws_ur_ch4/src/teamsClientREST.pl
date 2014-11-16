#!/usr/bin/perl

use strict;
use LWP;
use XML::XPath;
use constant true   =>  1;
use constant false  =>  0;
#use constant team   => -1; # one type of POST
#use constant player => -2; # another type of POST

# Create the user agent.
my $ua = LWP::UserAgent->new;

my $base_uri = 'http://localhost:8080/teams/restful';

## Test each of the HTTP verbs with query strings as appropriate.

# GET teams
my $request = $base_uri;
send_GET($request, false); # false means no query string

# GET teams?name=MarxBrothers
$request = $base_uri . '?name=MarxBrothers';
#send_GET($request, true);

# DELETE teams
$request = $base_uri;
#send_DELETE($request);

# DELETE teams?name=MarxBrothers
$request = $base_uri . '?name=MarxBrothers';
#send_DELETE($request);

# Test whether DELETE worked: should get 404 code
$request = $base_uri . '?name=MarxBrothers';
#send_GET($request, true);

$request = $base_uri;
send_POST($request);

#send_GET($request);

#send_PUT($request);

sub send_GET {
    my ($uri, $qs_flag) = @_; 

    # Send the request and get the response.
    my $req = HTTP::Request->new(GET => $uri);
    my $res = $ua->request($req);

    # Check for errors.
    if ($res->is_success) {
        parse_GET($res->content, $qs_flag); # Process raw XML on success
    }
    else {
        print $res->status_line, "\n";      # Print error code on failure
    }
}

# Print raw XML and the elements of interest.
sub parse_GET {
    my ($raw_xml, $qs_flag) = @_;
    print "\nThe raw XML response is:\n$raw_xml\n;;;\n";
    my $xp = XML::XPath->new(xml => $raw_xml);

    # GET all teams because there's no query string.
    # As proof of concept, extract the element data from the XML tags.
    if (!$qs_flag) {
	foreach my $node ($xp->find('//object/void/string')->get_nodelist) {
	    print $node->string_value, "\n";
	}
    }
}

sub send_POST {
    my ($uri, $team_or_player) = @_;

    my $xml = <<EOS;
      <create_team>
         <name>SmothersBrothers</name>
         <player>
           <name>Thomas</name>
           <nickname>Tom</nickname>
         </player>
         <player>
           <name>Richard</name>
           <nickname>Dickie</nickname>
         </player>
      </create_team>
EOS
   
    my $req = HTTP::Request->new(POST => $uri, ['Cargo' => $xml], $xml);

    print $req->content, "\n";

    my $res = $ua->request($req);

    # Check for errors.
    if ($res->is_success) {
        parse_POST($res->content);      # Process raw XML on success
    }
    else {
        print $res->status_line, "\n";  # Print error code on failure
    }
}

sub parse_POST {
    my $raw_xml = shift;
    print "\nResponse on POST: \n$raw_xml\n";
}

sub send_PUT {
    my ($uri, $team_or_player) = @_;

    my $xml = <<EOS;
      <change_team_name>
         <name>SmothersBrothers</name>
         <new>SmuthersBrothers</new>
      </change_team_name>
EOS
   
    my $req = HTTP::Request->new(PUT => $uri, ['Cargo' => $xml], $xml);
    my $res = $ua->request($req);

    # Check for errors.
    if ($res->is_success) {
        parse_PUT($res->content);      # Process raw XML on success
    }
    else {
        print $res->status_line, "\n"; # Print error code on failure
    }
}

sub parse_PUT {
    my $raw_xml = shift;
    print "\nResponse on PUT: \n$raw_xml\n";
}

sub send_DELETE {
    my ($uri, $qs_flag) = @_;

    # Send the request and get the response.
    my $req = HTTP::Request->new(DELETE => $uri);
    my $res = $ua->request($req);

    # Check for errors.
    if ($res->is_success) {
        parse_DELETE($res->content);   # Process raw XML on success
    }
    else {
        print $res->status_line, "\n"; # Print error code on failure
    }
}

sub parse_DELETE {
    my $raw_xml = shift;
    print "\nResponse on DELETE: \n$raw_xml\n;;;\n";
}
