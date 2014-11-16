#!/usr/bin/perl
use strict;
use LWP;
use XML::Parser;

# Create the user agent.
my $ua = LWP::UserAgent->new;

my $base_url =
    'http://api.search.yahoo.com:80/NewsSearchService/V1/newsSearch';

# Build some sample requests.
my $qs = '?appid=jaxws_restful_sample&type=all&results=10&' .
    'sort=date&language=en&query=java';
my $req_string = $base_url . $qs;
process($req_string);

sub process {
    my $req_string = shift;
    #print "\nThe GET request URL is: $req_string\n";
    
    # Send the request and get the response.
    my $req = HTTP::Request->new(GET => $req_string);
    my $res = $ua->request($req);
    
    # Check for errors.
    if ($res->is_success) {
	print "The raw XML is:\n", $res->content, "\n";
    }
    else {
	print $res->status_line, "\n";
    }
}
