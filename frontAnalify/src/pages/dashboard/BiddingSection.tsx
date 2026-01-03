import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Loader2, ChevronLeft, TrendingUp, Clock, DollarSign } from 'lucide-react';
import { biddingApi } from '@/services/api';
import type { SectionDTO, BidHistoryDTO } from '@/types';
import { useToast } from '@/hooks/use-toast';

export default function BiddingSection() {
  const { sectionId } = useParams<{ sectionId: string }>();
  const [section, setSection] = useState<SectionDTO | null>(null);
  const [bidHistory, setBidHistory] = useState<BidHistoryDTO[]>([]);
  const [bidAmount, setBidAmount] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    if (sectionId) {
      loadSection(Number(sectionId));
      loadBidHistory(Number(sectionId));
    }
  }, [sectionId]);

  const loadSection = async (id: number) => {
    try {
      setLoading(true);
      const data = await biddingApi.getSection(id) as SectionDTO;
      setSection(data);
      // Set default bid amount to slightly above current price
      setBidAmount((data.currentPrice + 50).toFixed(2));
    } catch (error) {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to load section',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  const loadBidHistory = async (id: number) => {
    try {
      const data = await biddingApi.getBidHistory(id);
      setBidHistory(data as BidHistoryDTO[]);
    } catch (error) {
      console.error('Failed to load bid history:', error);
    }
  };

  const handlePlaceBid = async () => {
    if (!section || !bidAmount) return;

    const amount = parseFloat(bidAmount);
    if (amount <= section.currentPrice) {
      toast({
        title: 'Invalid Bid',
        description: `Bid amount must be higher than current price (${section.currentPrice.toFixed(2)} DH)`,
        variant: 'destructive',
      });
      return;
    }

    try {
      setSubmitting(true);
      await biddingApi.placeBid({
        sectionId: section.sectionId,
        amount,
      });
      
      toast({
        title: 'Bid Placed!',
        description: `Your bid of ${amount.toFixed(2)} DH has been placed successfully`,
      });
      
      // Reload section and bid history
      await loadSection(section.sectionId);
      await loadBidHistory(section.sectionId);
    } catch (error) {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to place bid',
        variant: 'destructive',
      });
    } finally {
      setSubmitting(false);
    }
  };

  const getStatusBadge = (status: string) => {
    if (status === 'CLOSED') {
      return <Badge variant="secondary">Closed</Badge>;
    }
    if (status.startsWith('OPEN-BIDDEN')) {
      return <Badge variant="default">Active Bidding</Badge>;
    }
    return <Badge variant="outline">Open</Badge>;
  };

  const getBidStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <Badge variant="default">Winning</Badge>;
      case 'OUTBID':
        return <Badge variant="secondary">Outbid</Badge>;
      case 'WINNER':
        return <Badge className="bg-green-600">Winner</Badge>;
      default:
        return <Badge variant="outline">{status}</Badge>;
    }
  };

  if (loading || !section) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  const isClosed = section.status === 'CLOSED';
  const daysRemaining = Math.ceil(
    (new Date(section.dateDelai).getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24)
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="outline" onClick={() => navigate(-1)}>
          <ChevronLeft className="mr-2 h-4 w-4" />
          Back
        </Button>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">{section.sectionName}</h1>
          <p className="text-muted-foreground mt-1">
            Place your bid for this monthly contract
          </p>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Section Details */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Section Details</CardTitle>
                {getStatusBadge(section.status)}
              </div>
              {section.description && (
                <CardDescription>{section.description}</CardDescription>
              )}
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label className="text-muted-foreground">Base Price</Label>
                  <p className="text-2xl font-bold">{section.basePrice.toFixed(2)} DH</p>
                </div>
                <div className="space-y-2">
                  <Label className="text-muted-foreground">Current Price</Label>
                  <p className="text-2xl font-bold text-primary">{section.currentPrice.toFixed(2)} DH</p>
                </div>
              </div>

              <Separator />

              <div className="grid grid-cols-2 gap-4">
                <div className="flex items-center gap-2">
                  <Clock className="h-4 w-4 text-muted-foreground" />
                  <div>
                    <Label className="text-muted-foreground">Deadline</Label>
                    <p className="font-medium">{new Date(section.dateDelai).toLocaleDateString()}</p>
                    {!isClosed && daysRemaining > 0 && (
                      <p className="text-sm text-muted-foreground">{daysRemaining} days remaining</p>
                    )}
                  </div>
                </div>
                {section.winnerInvestorName && (
                  <div>
                    <Label className="text-muted-foreground">Current Winner</Label>
                    <p className="font-medium">{section.winnerInvestorName}</p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Bid History */}
          <Card>
            <CardHeader>
              <CardTitle>Bid History</CardTitle>
              <CardDescription>All bids placed on this section</CardDescription>
            </CardHeader>
            <CardContent>
              {bidHistory.length === 0 ? (
                <p className="text-center text-muted-foreground py-4">No bids yet</p>
              ) : (
                <div className="space-y-3">
                  {bidHistory.map((bid) => (
                    <div
                      key={bid.bidId}
                      className="flex items-center justify-between p-3 border rounded-lg"
                    >
                      <div>
                        <p className="font-medium">{bid.investorName}</p>
                        <p className="text-sm text-muted-foreground">
                          {new Date(bid.bidTime).toLocaleString()}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="font-bold">{bid.amount.toFixed(2)} DH</p>
                        {getBidStatusBadge(bid.status)}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Place Bid */}
        <div>
          <Card>
            <CardHeader>
              <CardTitle>Place Your Bid</CardTitle>
              <CardDescription>
                {isClosed
                  ? 'This section is closed for bidding'
                  : 'Enter amount higher than current price'}
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="bidAmount">Bid Amount (DH)</Label>
                <div className="relative">
                  <DollarSign className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    id="bidAmount"
                    type="number"
                    step="0.01"
                    value={bidAmount}
                    onChange={(e) => setBidAmount(e.target.value)}
                    disabled={isClosed}
                    className="pl-9"
                    placeholder={`Minimum: ${(section.currentPrice + 0.01).toFixed(2)}`}
                  />
                </div>
                <p className="text-sm text-muted-foreground">
                  Must be higher than {section.currentPrice.toFixed(2)} DH
                </p>
              </div>

              <Button
                className="w-full"
                onClick={handlePlaceBid}
                disabled={isClosed || submitting || !bidAmount}
              >
                {submitting ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Placing Bid...
                  </>
                ) : (
                  <>
                    <TrendingUp className="mr-2 h-4 w-4" />
                    Place Bid
                  </>
                )}
              </Button>

              {isClosed && (
                <p className="text-sm text-center text-muted-foreground">
                  Bidding has closed for this section
                </p>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
