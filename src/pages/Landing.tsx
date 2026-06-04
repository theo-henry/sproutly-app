import LandingSlider from "../components/LandingSlider";
import LandingSections from "../components/landing/LandingSections";

// Landing route = animated hero + scrollable info sections beneath.
// The hero auto-advances and exposes prev/next + dot pager, but it does
// NOT hijack the page wheel — users can scroll past it to read more.
export default function Landing() {
  return (
    <>
      <LandingSlider />
      <LandingSections />
    </>
  );
}
