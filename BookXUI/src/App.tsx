import { Routes, Route, useLocation } from 'react-router-dom';
import { AnimatePresence } from 'framer-motion';
import Navbar from './components/common/Navbar';
import Footer from './components/common/Footer';
import HomePage from './pages/HomePage';
import SeatPage from './pages/SeatPage';
import ConfirmationPage from './pages/ConfirmationPage';
import LookupPage from './pages/LookupPage';

export default function App() {
  const location = useLocation();

  return (
    <>
      <Navbar />
      <AnimatePresence mode="wait">
        <Routes location={location} key={location.pathname}>
          <Route path="/" element={<HomePage />} />
          <Route path="/show/:showId" element={<SeatPage />} />
          <Route path="/confirmation" element={<ConfirmationPage />} />
          <Route path="/lookup" element={<LookupPage />} />
        </Routes>
      </AnimatePresence>
      <Footer />
    </>
  );
}
