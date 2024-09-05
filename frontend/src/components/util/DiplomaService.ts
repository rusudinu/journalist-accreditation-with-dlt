import Diploma from '@/common/Diploma.ts';
import { randomPassingGradeGenerator } from '@/common/util/RandomService.ts';
import { v4 } from 'uuid';

export const generateRandomDiploma = (): Diploma => {
    return {
        diplomaId: v4(),
        studentId: v4(),
        emitter: 'MEN',
        title: 'BAC Exam',
        grade: randomPassingGradeGenerator().toString()
    }
}
