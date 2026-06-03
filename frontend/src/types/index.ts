export interface Activity {
  id: number;
  title: string;
  description: string;
  venueId: number;
  startTime: string;
  status: string;
  createdAt: string;
}

export interface SeatInfo {
  seatId: number;
  rowNum: number;
  colNum: number;
  areaTag: string;
  status: string;
  studentName: string | null;
  studentId: string | null;
  teamId: number | null;
  teamName: string | null;
  preferredArea: string | null;
  preferenceMatched: boolean;
}

export interface SeatMapResponse {
  activityId: number;
  activityTitle: string;
  venueName: string;
  totalRows: number;
  totalCols: number;
  hasWindowLeft: boolean;
  hasWindowRight: boolean;
  seats: SeatInfo[];
}

export interface RegistrationRequest {
  studentId: string;
  studentName: string;
  preferredArea: string;
  teamName?: string;
}

export interface Registration {
  id: number;
  userId: number;
  activityId: number;
  teamId: number | null;
  preferredArea: string;
  allocatedSeatId: number | null;
  registrationTime: string;
  status: string;
}

export interface Venue {
  id: number;
  name: string;
  totalRows: number;
  totalCols: number;
  hasWindowLeft: boolean;
  hasWindowRight: boolean;
}
